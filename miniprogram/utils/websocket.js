var app = getApp();
var socketTask = null;
var messageHandler = null;

function connect(onMessage) {
  messageHandler = onMessage;
  var token = app.globalData.token || wx.getStorageSync('token');
  var wsUrl = app.globalData.baseUrl.replace('https', 'wss').replace('http', 'ws');
  console.log('[WS] connecting to:', wsUrl + '/ws/chat?token=' + (token ? token.substring(0,20) : 'null'));

  socketTask = wx.connectSocket({
    url: wsUrl + '/ws/chat?token=' + token
  });

  socketTask.onOpen(function() {
    console.log('[WS] connected');
  });

  socketTask.onMessage(function(res) {
    try {
      var msg = JSON.parse(res.data);
      if (messageHandler) messageHandler(msg);
    } catch (e) {
      console.error('[WS] parse error:', e);
    }
  });

  socketTask.onError(function(err) {
    console.error('[WS] error:', err);
  });

  socketTask.onClose(function() {
    console.log('[WS] closed');
  });
}

function send(text) {
  if (socketTask) {
    socketTask.send({
      data: text,
      success: function() {},
      fail: function(err) { console.error('[WS] send fail:', err); }
    });
  }
}

function close() {
  if (socketTask) {
    socketTask.close();
    socketTask = null;
  }
}

module.exports = { connect: connect, send: send, close: close };
