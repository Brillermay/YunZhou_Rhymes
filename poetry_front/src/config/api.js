// 简单的API端口配置

// 当前使用的环境 - 只需要修改这一行
const CURRENT_ENV = 'local'; // 'local' 或 'production'

// 端口配置
const API_CONFIG = {
  local: 'http://localhost:8081',
  production: 'http://117.72.88.23:8080/api'
};

// 导出当前环境的API基础地址
export const API_BASE_URL = API_CONFIG[CURRENT_ENV];

// 默认导出
export default API_BASE_URL;