import Vue from 'vue'
import App from './App.vue'
import router from './router/router'
import ueboot from '../../src/index'
import ViewUI from 'view-design';
Vue.use(ViewUI);
// import style
import 'view-design/dist/styles/iview.css';
import 'font-awesome/less/font-awesome.less'

import userAvatarSrc from './assets/userImage.png'

Vue.use(ueboot)
ueboot.Config.setConfig({
  log: { level: 4 },
  sysTitle: 'UEBOOT测试',
  axios: { baseURL: '', unauthorizedUrl: '/#/login' },
  page_login: { successRouter: { name: '用户管理' } },
  page_main: {
    menuWidth: 250,
    dropdown:
      {
        avatar: {
          src: userAvatarSrc
        }
      }
  }
})

Vue.config.productionTip = false
/* eslint-disable no-new */
new Vue({
  router,
  render: h => h(App)
}).$mount("#app")

// 头像
function setUserImageSrc () {
  sessionStorage.setItem('login.userAvatar', 'http://0.0.0.0:8081/static/img/userImage.9228385.png')
}

setUserImageSrc()
