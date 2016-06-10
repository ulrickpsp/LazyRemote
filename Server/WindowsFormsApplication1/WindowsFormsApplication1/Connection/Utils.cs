using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace WindowsFormsApplication2.Connection
{
    class Utils
    {

        public static IPAddress getLocalIPAddress()
        {
            IPAddress ret = null;
            // Are we connected to the network?
            if (System.Net.NetworkInformation.NetworkInterface.GetIsNetworkAvailable())
            {
                // get a list of local addresses
                var addrs = Dns.GetHostAddresses(Dns.GetHostName());
                foreach (IPAddress ip in addrs)
                {
                    // is this an IPv4 address?
                    if (ip.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork)
                    {
                        // that's the one
                        ret = ip;
                        break;
                    }
                }
            }
            return ret;
        }

        public static String InvalidUrlFixer(String url)
        {

            var duplicateDetector = Regex.Matches(url, "http").Count;
            if (duplicateDetector > 1)
            {
                int index = url.IndexOf("http", url.IndexOf("http") + 1);
                url = url.Substring(0, index);
            }
            return url;
        }

        public static String getHtmlCode(String url)
        {

            String data = "";
            HttpWebRequest request;
            HttpWebResponse response;

            request = (HttpWebRequest)WebRequest.Create(url);
            response = (HttpWebResponse)request.GetResponse();

            if (response.StatusCode == HttpStatusCode.OK)
            {
                Stream receiveStream = response.GetResponseStream();
                StreamReader readStream = null;

                if (response.CharacterSet == null)
                {
                    readStream = new StreamReader(receiveStream);
                }
                else
                {
                    readStream = new StreamReader(receiveStream, Encoding.GetEncoding(response.CharacterSet));
                }

                data = readStream.ReadToEnd();

                response.Close();
                readStream.Close();
            }

            return data;

        }



    }
}
