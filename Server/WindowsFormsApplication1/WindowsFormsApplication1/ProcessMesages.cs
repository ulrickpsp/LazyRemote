using System;
using System.Diagnostics;
using System.IO;
using System.Net;
using System.Text;
using System.Text.RegularExpressions;
using CefSharp;
using CefSharp.WinForms;
using System.Windows.Forms;
using WindowsFormsApplication2.Connection.TCP;
using System.Timers;
using WindowsFormsApplication2.Connection;
using System.Net.Sockets;
using WindowsFormsApplication1;

namespace WindowsFormsApplication2
{
    public partial class ProcessMesages : Form
    {
        ChromiumWebBrowser myBrowser;
        String site;
        Volume volume;
        ProcessMesages processMesages;

        //Singleton Pattern
        private static ProcessMesages instance;
        public static ProcessMesages Instance
        {
            get
            {
                if (instance == null)
                {
                    instance = new ProcessMesages();
                }
                return instance;
            }
        }

        public ProcessMesages()
        {
            //Initialize CEF Chromium Browser
            InitializeComponent();
            Cef.Initialize();
            
            //Initialize Variables
            site = "";
            volume = new Volume();

            //Hide cursor and fullscreen
            this.WindowState = FormWindowState.Minimized;
        }


        //This function is executed each time a new command is received
        public void ProcessMessage(string url)
        {
            
            

            //Ensure url is well-formed
            url = Utils.InvalidUrlFixer(url);


            if (url.Contains("mitele"))
            {
                //Esto nos permite abrir el enlace en pantalla completa
                site = "mitele";
                String find = "data-popup-url=\"";
                url = Utils.getHtmlCode(url);
                url = url.Substring(url.IndexOf(find) + find.Length);
                url = "www.mitele.es/" + url.Substring(0, url.IndexOf("\""));


                Process.Start(url);

            }

            else if (url.Contains("youtu"))
            {
                //TODO: [Baja] Algunos videos no se reproducen por tener contenido protegido. Tratar de resolver. 

                site = "youtube";

                

                Process.Start(url);
  
            }

            else if (url.Contains("Soccer"))
            {
                url = url.Substring(url.IndexOf("_"));
                var platform = url.Substring(url.IndexOf("_") + 1, url.IndexOf(":") - 1);
                var channel = url.Substring(url.IndexOf(":") + 1);
                var html = Utils.getHtmlCode("http://arenavision.in/av" + channel);


                if (platform.Equals("Sopcast"))
                {

                    var string_sopcast = "Click <a href=\"";
                    url = html.Substring(html.IndexOf(string_sopcast) + string_sopcast.Length);
                    url = url.Substring(0, url.IndexOf("\" target=\"_blank\">aquí") - 2);

                    Process.Start(url);
                }
                else
                {
                    var string_acestream = "Click<a href=\"";
                    url = html.Substring(html.IndexOf(string_acestream) + string_acestream.Length);
                    url = url.Substring(0, url.IndexOf("target=\"_blank\">aqui") - 2);


                    ProcessStartInfo startInfo = new ProcessStartInfo(url);
                    startInfo.WindowStyle = ProcessWindowStyle.Maximized;
                    Process.Start(startInfo);
                }
            }


            else if (url.Contains("VOLUME"))
            {
                if (url.Equals("VOLUME"))
                {
                    var currentVolume = volume.MasterVolume;
                    TCPmanager.Send((currentVolume * 100).ToString("0"));
                }
                else
                {
                    var desiredVolume = float.Parse(url.Substring(url.IndexOf("_") + 1)) / 100;
                    volume.MasterVolume = desiredVolume;
                }
            }

            else if (url.Contains("MOUSE"))
            {
                if (url.Contains("LEFTCLICK"))
                {
                    Mouse.PerformLeftClick();
                }
                else if (url.Contains("RIGHTCLICK"))
                {
                    Mouse.PerformRightClick();
                }

                else
                {

                    Boolean endReached = false;
                    while (endReached == false)
                    {

                        try
                        {
                            var helper = url.Substring(6);
                            var nextPointXY = helper.Substring(0, helper.IndexOf("|"));
                            var positionX = int.Parse(nextPointXY.Substring(0, nextPointXY.IndexOf("?")));
                            var positionY = int.Parse(nextPointXY.Substring(nextPointXY.IndexOf("?") + 1));
                            Mouse.SetCursorAtNewPosition(positionX, positionY);
                            url = url.Substring(url.IndexOf("|") + 1);
                        }
                        catch (Exception ex)
                        {
                            endReached = true;
                        }
                    }

                }
            }

            else if (url.Contains("SHUTDOWNPC"))
            {
                String shutDownArgument = "";

                if (url.Contains("CANCEL"))
                    shutDownArgument = "/a";
                else
                    shutDownArgument = "/s /t " + url.Substring(url.IndexOf("_") + 1);

                Process.Start("shutdown", shutDownArgument);
            }

            else {

                this.Invoke((MethodInvoker)delegate
                {
                    WindowState = FormWindowState.Maximized;

                    if (myBrowser == null)
                    {
                        myBrowser = new ChromiumWebBrowser(url);
                        myBrowser.LoadingStateChanged += LoadingStateChangedEvent;
                        this.Controls.Add(myBrowser);
                    }
                    else
                    {

                        myBrowser.Load(url);
                    }
                });
            }
        }

        public void LoadingStateChangedEvent(object sender, LoadingStateChangedEventArgs e)
        {
            if (e.IsLoading == false)
            {
                if (site.Equals("mitele"))
                {
                    myBrowser.ExecuteScriptAsync("$('div').eq(0).css({ position:'absolute', top:0, left:0, width:'100%', height:'100%'});");
                }
                else if (site.Equals("youtube"))
                {
                    myBrowser.ExecuteScriptAsync("$('.ytp-iv-video-content').css({ position:'absolute', top:0, left:0, width:'100%', height:'100%'});");
                }
            }
        }

        private void ProcessMesages_FormClosing(object sender, FormClosingEventArgs e)
        {
            this.Hide();
            this.WindowState = FormWindowState.Minimized;
        }
    }
}
