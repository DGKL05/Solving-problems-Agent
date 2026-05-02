const app = getApp();
const auth = require('./auth');

let socketTask = null;
let messageHandler = null;

function connect(onMessage) {
  messageHandler = onMessage;
  const token = auth.getToken();

  socketTask = wx.connectSocket({
    url: `${app.globalData.baseUrl.replace('https', 'wss')}/ws/chat?token=${token}`,
    success() {
      console.log('WebSocket connecting...');
    }
  });

  socketTask.onOpen(() => {
    console.log('WebSocket connected');
  });

  socketTask.onMessage((res) => {
    try {
      const msg = JSON.parse(res.data);
      if (messageHandler) messageHandler(msg);
    } catch (e) {
      console.error('WebSocket parse error:', e);
    }
  });

  socketTask.onError((err) => {
    console.error('WebSocket error:', err);
  });

  socketTask.onClose(() => {
    console.log('WebSocket closed');
  });
}

function send(text) {
  if (socketTask) {
    socketTask.send({
      data: text,
      success() {},
      fail(err) {
        console.error('Send failed:', err);
      }
    });
  }
}

function close() {
  if (socketTask) {
    socketTask.close();
    socketTask = null;
  }
}

module.exports = { connect, send, close };
