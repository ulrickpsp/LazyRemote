//Wrapper class for getting volume information

using System;
using System.Collections.Generic;
using System.Text;
using System.Runtime.InteropServices;

//FROM "Max.Brugnerotto" http://forums.microsoft.com/MSDN/ShowPost.aspx?PostID=1287868&SiteID=1
//Used by VolumeSensor to access vista master volume
namespace WindowsFormsApplication2
{
    /// <summary>
    /// This class allows to read and change the master volume in Vista (End Point Volume)
    /// </summary>
    class Volume
    {
        #region Interface to COM objects
        const int DEVICE_STATE_ACTIVE = 0x00000001;
        const int DEVICE_STATE_DISABLE = 0x00000002;
        const int DEVICE_STATE_NOTPRESENT = 0x00000004;
        const int DEVICE_STATE_UNPLUGGED = 0x00000008;
        const int DEVICE_STATEMASK_ALL = 0x0000000f;
        [DllImport("ole32.Dll")]
        static public extern uint CoCreateInstance(ref Guid clsid,
        [MarshalAs(UnmanagedType.IUnknown)] object inner,
        uint context,
        ref Guid uuid,
        [MarshalAs(UnmanagedType.IUnknown)] out object rReturnedComObject);
        // C Header file : Include Mmdeviceapi.h (Windows Vista SDK)
        [Guid("5CDF2C82-841E-4546-9722-0CF74078229A"),
        InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        public interface IAudioEndpointVolume
        {
            int RegisterControlChangeNotify(DelegateMixerChange pNotify);
            int UnregisterControlChangeNotify(DelegateMixerChange pNotify);
            int GetChannelCount(ref uint pnChannelCount);
            int SetMasterVolumeLevel(float fLevelDB, Guid pguidEventContext);
            int SetMasterVolumeLevelScalar(float fLevel, Guid pguidEventContext);
            int GetMasterVolumeLevel(ref float pfLevelDB);
            int GetMasterVolumeLevelScalar(ref float pfLevel);
            int SetChannelVolumeLevel(uint nChannel, float fLevelDB, Guid pguidEventContext);
            int SetChannelVolumeLevelScalar(uint nChannel, float fLevel, Guid pguidEventContext);
            int GetChannelVolumeLevel(uint nChannel, ref float pfLevelDB);
            int GetChannelVolumeLevelScalar(uint nChannel, ref float pfLevel);
            int SetMute(bool bMute, Guid pguidEventContext);
            int GetMute(ref bool pbMute);
            int GetVolumeStepInfo(ref uint pnStep, ref uint pnStepCount);
            int VolumeStepUp(Guid pguidEventContext);
            int VolumeStepDown(Guid pguidEventContext);
            int QueryHardwareSupport(ref uint pdwHardwareSupportMask);
            int GetVolumeRange(ref float pflVolumeMindB, ref float pflVolumeMaxdB, ref float pflVolumeIncrementdB);
        }

        [Guid("0BD7A1BE-7A1A-44DB-8397-CC5392387B5E"),
        InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        public interface IMMDeviceCollection
        {
            int GetCount(ref uint pcDevices);
            int Item(uint nDevice, ref IntPtr ppDevice);
        }

        [Guid("D666063F-1587-4E43-81F1-B948E807363F"),
        InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        public interface IMMDevice
        {
            int Activate(ref Guid iid, uint dwClsCtx, IntPtr pActivationParams, ref IntPtr ppInterface);
            int OpenPropertyStore(int stgmAccess, ref IntPtr ppProperties);
            int GetId(ref string ppstrId);
            int GetState(ref int pdwState);
        }

        [Guid("A95664D2-9614-4F35-A746-DE8DB63617E6"),
        InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
        public interface IMMDeviceEnumerator
        {
            int EnumAudioEndpoints(EDataFlow dataFlow, int dwStateMask, ref IntPtr ppDevices);
            int GetDefaultAudioEndpoint(EDataFlow dataFlow, ERole role, ref IntPtr ppEndpoint);
            int GetDevice(string pwstrId, ref IntPtr ppDevice);
            int RegisterEndpointNotificationCallback(IntPtr pClient);
            int UnregisterEndpointNotificationCallback(IntPtr pClient);
        }

        [Flags]
        enum CLSCTX : uint
        {
            CLSCTX_INPROC_SERVER = 0x1,
            CLSCTX_INPROC_HANDLER = 0x2,
            CLSCTX_LOCAL_SERVER = 0x4,
            CLSCTX_INPROC_SERVER16 = 0x8,
            CLSCTX_REMOTE_SERVER = 0x10,
            CLSCTX_INPROC_HANDLER16 = 0x20,
            CLSCTX_RESERVED1 = 0x40,
            CLSCTX_RESERVED2 = 0x80,
            CLSCTX_RESERVED3 = 0x100,
            CLSCTX_RESERVED4 = 0x200,
            CLSCTX_NO_CODE_DOWNLOAD = 0x400,
            CLSCTX_RESERVED5 = 0x800,
            CLSCTX_NO_CUSTOM_MARSHAL = 0x1000,
            CLSCTX_ENABLE_CODE_DOWNLOAD = 0x2000,
            CLSCTX_NO_FAILURE_LOG = 0x4000,
            CLSCTX_DISABLE_AAA = 0x8000,
            CLSCTX_ENABLE_AAA = 0x10000,
            CLSCTX_FROM_DEFAULT_CONTEXT = 0x20000,
            CLSCTX_INPROC = CLSCTX_INPROC_SERVER | CLSCTX_INPROC_HANDLER,
            CLSCTX_SERVER = CLSCTX_INPROC_SERVER | CLSCTX_LOCAL_SERVER | CLSCTX_REMOTE_SERVER,
            CLSCTX_ALL = CLSCTX_SERVER | CLSCTX_INPROC_HANDLER
        }

        public enum EDataFlow
        {
            eRender,
            eCapture,
            eAll,
            EDataFlow_enum_count
        }

        public enum ERole
        {
            eConsole,
            eMultimedia,
            eCommunications,
            ERole_enum_count
        }

        #endregion



        // Private internal var
        object oEnumerator = null;
        IMMDeviceEnumerator iMde = null;
        object oDevice = null;
        IMMDevice imd = null;
        object oEndPoint = null;
        IAudioEndpointVolume iAudioEndpoint = null;

        public delegate void DelegateMixerChange();
        public delegate void MixerChangedEventHandler();

        #region Class Constructor and Dispose public methods
        /// <summary>
        /// Constructor
        /// </summary>

        public Volume()
        {
            const uint CLSCTX_INPROC_SERVER = 1;
            Guid clsid = new Guid("BCDE0395-E52F-467C-8E3D-C4579291692E");
            Guid IID_IUnknown = new Guid("00000000-0000-0000-C000-000000000046");
            oEnumerator = null;
            uint hResult = CoCreateInstance(ref clsid, null, CLSCTX_INPROC_SERVER, ref IID_IUnknown, out oEnumerator);
            if (hResult != 0 || oEnumerator == null)
            {
                throw new Exception("CoCreateInstance() pInvoke failed");
            }
            iMde = oEnumerator as IMMDeviceEnumerator;
            if (iMde == null)
            {
                throw new Exception("COM cast failed to IMMDeviceEnumerator");
            }
            IntPtr pDevice = IntPtr.Zero;
            int retVal = iMde.GetDefaultAudioEndpoint(EDataFlow.eRender, ERole.eConsole, ref pDevice);
            if (retVal != 0)
            {
                throw new Exception("IMMDeviceEnumerator.GetDefaultAudioEndpoint()");
            }
            int dwStateMask = DEVICE_STATE_ACTIVE | DEVICE_STATE_NOTPRESENT | DEVICE_STATE_UNPLUGGED;
            IntPtr pCollection = IntPtr.Zero;
            retVal = iMde.EnumAudioEndpoints(EDataFlow.eRender, dwStateMask, ref pCollection);
            if (retVal != 0)
            {
                throw new Exception("IMMDeviceEnumerator.EnumAudioEndpoints()");
            }
            oDevice = System.Runtime.InteropServices.Marshal.GetObjectForIUnknown(pDevice);
            imd = oDevice as IMMDevice;
            if (imd == null)
            {
                throw new Exception("COM cast failed to IMMDevice");
            }
            Guid iid = new Guid("5CDF2C82-841E-4546-9722-0CF74078229A");
            uint dwClsCtx = (uint)CLSCTX.CLSCTX_ALL;
            IntPtr pActivationParams = IntPtr.Zero;
            IntPtr pEndPoint = IntPtr.Zero;
            retVal = imd.Activate(ref iid, dwClsCtx, pActivationParams, ref pEndPoint);
            if (retVal != 0)
            {
                throw new Exception("IMMDevice.Activate()");
            }
            oEndPoint = System.Runtime.InteropServices.Marshal.GetObjectForIUnknown(pEndPoint);
            iAudioEndpoint = oEndPoint as IAudioEndpointVolume;
            if (iAudioEndpoint == null)
            {
                throw new Exception("COM cast failed to IAudioEndpointVolume");
            }
        }
        /// <summary>
        /// Call this method to release all com objetcs
        /// </summary>
        public virtual void Dispose()
        {
            if (iAudioEndpoint != null)
            {
                System.Runtime.InteropServices.Marshal.ReleaseComObject(iAudioEndpoint);
                iAudioEndpoint = null;
            }
            if (oEndPoint != null)
            {
                System.Runtime.InteropServices.Marshal.ReleaseComObject(oEndPoint);
                oEndPoint = null;
            }
            if (imd != null)
            {
                System.Runtime.InteropServices.Marshal.ReleaseComObject(imd);
                imd = null;
            }
            if (oDevice != null)
            {
                System.Runtime.InteropServices.Marshal.ReleaseComObject(oDevice);
                oDevice = null;
            }
            if (iMde != null)
            {
                System.Runtime.InteropServices.Marshal.ReleaseComObject(iMde);
                iMde = null;
            }
            if (oEnumerator != null)
            {
                System.Runtime.InteropServices.Marshal.ReleaseComObject(oEnumerator);
                oEnumerator = null;
            }
        }
        #endregion

        #region Private internal functions

        private void MixerChange()
        {
        }

        #endregion

        #region Public properties
        /// <summary>
        /// Get/set the master mute. WARNING : The set mute do NOT work!
        /// </summary>
        public bool Mute
        {
            get
            {
                bool mute = false;
                int retVal = iAudioEndpoint.GetMute(ref mute);
                if (retVal != 0)
                {
                    throw new Exception("IAudioEndpointVolume.GetMute() failed!");
                }
                return mute;
            }

            set
            {
                Guid nullGuid = Guid.Empty;
                bool mute = value;

                int retVal = iAudioEndpoint.SetMute(mute, nullGuid);
                if (retVal != 0)
                {
                    throw new Exception("IAudioEndpointVolume.SetMute() failed!");
                }
            }
        }

        /// <summary>
        /// Get/set the master volume level. Valid range is from 0.00F (0%) to 1.00F (100%).
        /// </summary>
        public float MasterVolume
        {
            get
            {
                float level = 0.0F;
                int retVal = iAudioEndpoint.GetMasterVolumeLevelScalar(ref level);
                if (retVal != 0)
                {
                    throw new Exception("IAudioEndpointVolume.GetMasterVolumeLevelScalar()");
                }
                return level;
            }

            set
            {
                float level = value;
                Guid nullGuid;
                nullGuid = Guid.Empty;
                int retVal = iAudioEndpoint.SetMasterVolumeLevelScalar(level, nullGuid);
                if (retVal != 0)
                {
                    throw new Exception("IAudioEndpointVolume.SetMasterVolumeLevelScalar()");
                }
            }
        }

        #endregion

        #region Public Methods
        /// <summary>
        /// Increase the master volume
        /// </summary>
        public void VolumeUp()
        {
            Guid nullGuid;
            nullGuid = Guid.Empty;
            int retVal = iAudioEndpoint.VolumeStepUp(nullGuid);
            if (retVal != 0)
            {
                throw new Exception("IAudioEndpointVolume.SetMute()");
            }
        }

        /// <summary>
        /// Decrease the master volume
        /// </summary>
        public void VolumeDown()
        {
            Guid nullGuid;
            nullGuid = Guid.Empty;
            int retVal = iAudioEndpoint.VolumeStepDown(nullGuid);
        }
        #endregion
        #region GetSystemMute Example Function
        #endregion
    }
}