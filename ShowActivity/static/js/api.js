// API 配置文件
const BASE_URL = 'http://localhost:8080';

const API = {
  // 活动相关接口
  ACTIVITIES: BASE_URL + '/activities',
  ACTIVITIES_ADD: BASE_URL + '/activities/add',
  ACTIVITIES_SHOW: BASE_URL + '/activities/show',
  ACTIVITIES_JOINED: BASE_URL + '/activities/joined',
  ACTIVITIES_UPDATE: BASE_URL + '/activities/update',
  ACTIVITIES_SET_PRICE: BASE_URL + '/activities/setPrice',
  ACTIVITIES_DELETE: BASE_URL + '/activities/delete',
  ACTIVITIES_JOIN: BASE_URL + '/activities/join',
  ACTIVITIES_QUIT: BASE_URL + '/activities/quit',
  
  // 用户相关接口
  LOGIN: BASE_URL + '/auth/login',
  REGISTER: BASE_URL + '/auth/register',
  AUTH_ME: BASE_URL + '/auth/me',

  // admin endpoints
  ADMIN_ACTIVITIES_LIST: BASE_URL + '/admin/activities/list',
  ADMIN_ACTIVITIES_UPDATE: BASE_URL + '/admin/activities/update',
  ADMIN_ACTIVITIES_DELETE: BASE_URL + '/admin/activities/delete',
  ADMIN_ACTIVITIES_SET_PRICE: BASE_URL + '/admin/activities/setPrice',
  ADMIN_USERS_LIST: BASE_URL + '/admin/users/list',
  ADMIN_USERS_BAN: BASE_URL + '/admin/users/ban',
  ADMIN_USERS_UNBAN: BASE_URL + '/admin/users/unban',

  // 抽签接口
  DRAWS_CREATE: BASE_URL + '/draws/create',
  DRAWS_JOIN: BASE_URL + '/draws/join',
  DRAWS_DETAIL: BASE_URL + '/draws/detail',
  DRAWS_LIST: BASE_URL + '/draws/list',
  DRAWS_DISSOLVE: BASE_URL + '/draws/dissolve',

  // 管理员抽签接口
  ADMIN_DRAWS_LIST: BASE_URL + '/admin/draws/list',
  ADMIN_DRAWS_DETAIL: BASE_URL + '/admin/draws/detail',
  ADMIN_DRAWS_DISSOLVE: BASE_URL + '/admin/draws/dissolve'
};

// 为了更好地兼容微信小程序环境，使用 module.exports
module.exports = API;

// 同时保留 default export 以支持 ES6 导入
module.exports.default = API;
