/**
 * localStorage数据操作工具类
 * 实现按键值读取保存数据，包括增删查改和一键清空
 */

// 保存数据
export const saveData = (key, value) => {
    try {
        // 将对象或数组转换为JSON字符串
        const stringData = typeof value === 'object' ? JSON.stringify(value) : value;
        localStorage.setItem(key, stringData);
        return true;
    } catch (error) {
        console.error('保存数据失败:', error);
        return false;
    }
};

// 读取数据
export const getData = (key) => {
    try {
        const data = localStorage.getItem(key);
        // 尝试将JSON字符串解析为对象
        try {
            return JSON.parse(data);
        } catch (e) {
            // 如果解析失败，则返回原始数据
            return data;
        }
    } catch (error) {
        console.error('读取数据失败:', error);
        return null;
    }
};

// 更新数据
export const updateData = (key, value) => {
    // 更新操作实际上就是保存操作
    return saveData(key, value);
};

// 删除单个数据
export const removeData = (key) => {
    try {
        localStorage.removeItem(key);
        return true;
    } catch (error) {
        console.error('删除数据失败:', error);
        return false;
    }
};

// 检查数据是否存在
export const hasData = (key) => {
    return localStorage.getItem(key) !== null;
};

// 一键清空所有数据
export const clearAllData = () => {
    try {
        localStorage.clear();
        return true;
    } catch (error) {
        console.error('清空数据失败:', error);
        return false;
    }
};