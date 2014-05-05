var SAAgent = null;
	var SASocket = null;
	var CHANNELID = 104;
	var ProviderAppName = "GearDemoApp";


	function onerror(err) {
		console.log(err);
		setError(err);
	}

	function test(channelId, data) {
		var message = JSON.parse(data);
	}
	
	var agentCallback = {
		onconnect : function(socket) {
			SASocket = socket;
			SASocket.setDataReceiveListener(onreceive);
			
			SASocket.setSocketStatusListener(function(reason){
				console.log("Service connection lost, Reason : [" + reason + "]");
				disconnect();
			});
			
			setStatus("Connection established with RemotePeer");
			
		},
		onerror : onerror
	};

	var peerAgentFindCallback = {
		onpeeragentfound : function(peerAgent) {
			try {
				if (peerAgent.appName == ProviderAppName) {
					SAAgent.setServiceConnectionListener(agentCallback);
					SAAgent.requestServiceConnection(peerAgent);
					
				} else {
					setError("Not expected app!! : " + peerAgent.appName);
				}
			} catch(err) {
				console.log("exception [" + err.name + "] msg[" + err.message + "]");
			}
		},
		onerror : onerror
	}

	function onsuccess(agents) {
		try {
			if (agents.length > 0) {
				SAAgent = agents[0];
				
				SAAgent.setPeerAgentFindListener(peerAgentFindCallback);
				SAAgent.findPeerAgents();
			} else {
				setError("Not found SAAgent!!");
			}
		} catch(err) {
			console.log("exception [" + err.name + "] msg[" + err.message + "]");
			setError(err);
		}
	}

	function connect() {
		disconnect()
		
		if (SASocket) {
			SASocket = null;
	    }
		try {
			webapis.sa.requestSAAgent(onsuccess, onerror);
		} catch(err) {
			console.log("exception [" + err.name + "] msg[" + err.message + "]");
		}
	}

	function disconnect() {
		try {
			if (SASocket != null) {
				SASocket.close();
				SASocket = null;
				setStatus("closeConnection");
				
				$('#call').hide();
				$('#alert').hide();
				$('#idle').show();
			}
		} catch(err) {
			console.log("exception [" + err.name + "] msg[" + err.message + "]");
		}
	}

	
	function fetch() {
		try {
			SASocket.setDataReceiveListener(onreceive);
			SASocket.sendData(CHANNELID, "Hello Accessory!");
		} catch(err) {
			console.log("exception [" + err.name + "] msg[" + err.message + "]");
		}
	}

