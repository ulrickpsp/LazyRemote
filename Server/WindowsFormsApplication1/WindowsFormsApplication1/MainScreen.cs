using System;
using System.Threading.Tasks;
using System.Timers;
using System.Windows.Forms;
using WindowsFormsApplication1;
using WindowsFormsApplication2.Connection.TCP;
using WindowsFormsApplication2.Connection.UDP;

namespace WindowsFormsApplication2
{
    public partial class MainScreen : Form
    {
        UDPmanager udpManager;
        ProcessMesages processMesages;

        
        public MainScreen()
        {
            InitializeComponent();

            //Get instances of both classes
            udpManager = UDPmanager.Instance;
            processMesages = ProcessMesages.Instance;

            this.WindowState = FormWindowState.Minimized;
            notifyIcon1.Visible = true;
            notifyIcon1.BalloonTipText = "Application Minimized";
            notifyIcon1.ShowBalloonTip(500);

            Application.ApplicationExit += new EventHandler(this.OnApplicationExit);

        }

        private void OnApplicationExit(object sender, EventArgs e)
        {
            notifyIcon1.Dispose();
        }

        private void Form1_Load(object sender, EventArgs e)
        {
            Task.Run(() => {
                udpManager.startUDPListener();
            });
            Task.Run(() => {
                TCPmanager.StartListening();
            });

            processMesages.Show();
            Hide();
        }


        private void notifyIcon1_MouseClick(object sender, MouseEventArgs e)
        {
            Show();
            WindowState = FormWindowState.Normal;
        }

        private void notifyIcon1_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            Show();
            WindowState = FormWindowState.Normal;
        }

        private void MainScreen_Resize(object sender, EventArgs e)
        {
            if (FormWindowState.Minimized == WindowState)
                Hide();
        }
    }
}
