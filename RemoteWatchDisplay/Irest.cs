using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Text;

namespace RemoteWatchDisplay
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the interface name "Irest" in both code and config file together.
    [ServiceContract]
    public interface IRest
    {
        [OperationContract]
        [WebInvoke(Method = "POST",
        ResponseFormat = WebMessageFormat.Json,
        RequestFormat = WebMessageFormat.Json,
        BodyStyle = WebMessageBodyStyle.WrappedRequest)]
        void PostData(Stream data);

        [OperationContract]
               [WebInvoke(Method = "GET",ResponseFormat = WebMessageFormat.Xml, 
                  BodyStyle = WebMessageBodyStyle.Bare)]
        Stream GetData();
    }
}
