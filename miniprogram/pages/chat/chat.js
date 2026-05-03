var api = require('../../utils/api');
var websocket = require('../../utils/websocket');

function doLogin(callback) {
  wx.login({
    success: function(res) {
      if (!res.code) { wx.showToast({ title: '登录失败', icon: 'none' }); return; }
      wx.request({
        url: getApp().globalData.baseUrl + '/api/auth/login',
        method: 'POST',
        data: { code: res.code },
        success: function(resp) {
          var body = resp.data;
          if (typeof body === 'string') body = JSON.parse(body);
          if (body.token) {
            wx.setStorageSync('token', body.token);
            wx.setStorageSync('userId', body.userId);
            getApp().globalData.token = body.token;
            getApp().globalData.userId = body.userId;
            if (callback) callback();
          }
        },
        fail: function() { wx.showToast({ title: '网络错误', icon: 'none' }); }
      });
    }
  });
}

Page({
  data: {
    messages: [],
    inputText: '',
    sessionId: null,
    showSubjectPicker: false,
    pendingImagePath: null
  },

  onLoad: function() {
    var that = this;
    doLogin(function() {
      websocket.connect(function(msg) {
        that.handleWsMessage(msg);
      });
    });
  },

  onUnload: function() {
    websocket.close();
  },

  handleWsMessage: function(msg) {
    var messages = this.data.messages;
    var last = messages.length > 0 ? messages[messages.length - 1] : null;

    if (msg.type === 'connected') {
      this.setData({ sessionId: msg.sessionId });
    } else if (msg.type === 'solve-start') {
      messages.push({ role: 'assistant', content: '', time: new Date(), streaming: true, isSolution: true });
      this.setData({ messages: messages });
    } else if (msg.type === 'solve-chunk') {
      if (last && last.streaming) {
        last.content += msg.chunk;
        this.setData({ messages: messages });
      }
    } else if (msg.type === 'solve-done') {
      if (last && last.streaming) {
        last.streaming = false;
        this.setData({ messages: messages });
      }
    } else if (msg.type === 'solve-error') {
      if (last && last.streaming) {
        last.content += '\n\n[解题失败: ' + (msg.message || '未知错误') + ']';
        last.streaming = false;
        this.setData({ messages: messages });
      }
    } else if (msg.type === 'message') {
      messages.push({ role: 'assistant', content: msg.content, time: new Date() });
      this.setData({ messages: messages });
    }
  },

  onInput: function(e) {
    this.setData({ inputText: e.detail.value });
  },

  sendMessage: function() {
    var text = this.data.inputText.trim();
    if (!text) return;

    var messages = this.data.messages;
    messages.push({ role: 'user', content: text, time: new Date() });
    this.setData({ messages: messages, inputText: '' });

    websocket.send(JSON.stringify({ type: 'text', content: text }));
  },

  chooseImage: function() {
    var that = this;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: function(res) {
        that.setData({
          showSubjectPicker: true,
          pendingImagePath: res.tempFiles[0].tempFilePath
        });
      }
    });
  },

  onSubjectSelect: function(e) {
    var subjectType = e.currentTarget.dataset.type;
    if (!subjectType) {
      this.setData({ showSubjectPicker: false, pendingImagePath: null });
      return;
    }

    var filePath = this.data.pendingImagePath;
    var sessionId = this.data.sessionId;
    this.setData({ showSubjectPicker: false, pendingImagePath: null });

    var messages = this.data.messages;
    messages.push({ role: 'user', content: '[图片]', time: new Date(), isImage: true });
    this.setData({ messages: messages });

    wx.showLoading({ title: '识别中...' });

    var that = this;
    api.upload(filePath, subjectType, sessionId).then(function(res) {
      wx.hideLoading();
      if (res.code === 200) {
        var data = res.data;
        messages.push({
          role: 'assistant',
          content: '📷 识别结果：\n' + data.cleanedText,
          time: new Date()
        });
        that.setData({ messages: messages });

        // Send solve command via WebSocket for streaming
        websocket.send(JSON.stringify({
          type: 'solve',
          text: data.cleanedText,
          subjectType: data.subjectType || subjectType
        }));
      } else {
        wx.showToast({ title: '识别失败', icon: 'error' });
      }
    }).catch(function(err) {
      wx.hideLoading();
      wx.showToast({ title: '上传失败', icon: 'error' });
    });
  }
});
