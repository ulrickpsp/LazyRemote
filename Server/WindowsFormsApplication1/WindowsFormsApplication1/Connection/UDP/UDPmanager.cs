using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using WindowsFormsApplication2.Connection.TCP;

namespace WindowsFormsApplication2.Connection.UDP
{
    class UDPmanager
    {
        static UdpClient udpClient;
        List<object> udpParams;
        Boolean finishUDP;

        //Singleton pattern to allow only one instance of this class
        private static UDPmanager instance;
        public static UDPmanager Instance
        {
            get
            {
                if (instance == null)
                {
                    instance = new UDPmanager();
                }
                return instance;
            }
        }

        private UDPmanager() {

            //Create holder for udpParameters
            udpParams = new List<object>();

            //Initialize UDPclient
            udpClient = new UdpClient(UDPparameters.PORT);
            udpClient.Client.SendTimeout = UDPparameters.SEND_TIMEOUT;

        }
        

        ////////////////////////////////////////////////
        //                  UDP LISTENER
        ////////////////////////////////////////////////
        public void startUDPListener()
        {
            var port = UDPparameters.PORT;
            byte[] receivedData = null;
            IPEndPoint RemoteIpEndPoint = null;

            while (finishUDP == false)
            {
                try
                {
                    Console.WriteLine("UDP: Waiting for a client...");
                    RemoteIpEndPoint = new IPEndPoint(IPAddress.Any, 0);
                    receivedData = udpClient.Receive(ref RemoteIpEndPoint);
                    finishUDP = true;
                }
                catch (SocketException)
                {
                    //Receive Timeout reached. Needed in order to be able to report progress.
                }
            }

            //Get System IP
            var localIP = Utils.getLocalIPAddress();

            //Convert bytes[] to String
            var receivedString = Encoding.Default.GetString((Byte[])receivedData);


            if (receivedString.Contains(UDPmessages.UDPdiscovery))
            {
                Thread.Sleep(500);
                var destinationIP = receivedString.Substring(receivedString.IndexOf("_") + 1);
                Console.WriteLine("UDP: Data from client: " + receivedString.Substring(receivedString.IndexOf("_") + 1));
                IPEndPoint ip = new IPEndPoint(IPAddress.Parse(destinationIP), (int)port);
                byte[] bytes = Encoding.UTF8.GetBytes("ServerID_" + localIP + ":" + TCPconfiguration.PORT + "?");
                udpClient.Send(bytes, bytes.Length, ip);
                udpClient.Close();
            }
        }
    }
}
