using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;

namespace RemoteWatchDisplay
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "rest" in code, svc and config file together.
    // NOTE: In order to launch WCF Test Client for testing this service, please select rest.svc or rest.svc.cs at the Solution Explorer and start debugging.
    public class rest : IRest
    {
        static string s_data = "{'hello':'world'}";

        public void PostData(Stream data)
        {
            StreamReader sre = new StreamReader(data);
            string postData = sre.ReadToEnd();
            s_data = postData;
            Console.Write(postData);
        }


        public Stream GetData()
        {
            byte[] byteArray = Encoding.UTF8.GetBytes(s_data);
            MemoryStream stream = new MemoryStream(byteArray);
            return stream;
            //return s_data;
        }
    }
}
