/*
 * 图标上传相关的公共逻辑。
 * 原本在 app / channel / robot 三个页面里各写了一份一模一样的实现。
 */

// 默认部署方式下前后端同源，用相对路径即可；
// 前后端分开部署时改成后端地址，例如 'http://localhost:8880/api/application/media/upload/'
export const UPLOAD_MEDIA_URL = '/api/application/media/upload/';

const ACCEPTED_TYPES = ['image/jpeg', 'image/png'];
const MAX_SIZE_MB = 2;

/**
 * 上传前校验图片格式和大小。
 * @param {File} file 待上传文件
 * @param {(msg: string) => void} onError 校验不通过时的提示回调
 * @returns {boolean} 是否允许上传
 */
export function validatePortrait(file, onError) {
    if (!ACCEPTED_TYPES.includes(file.type)) {
        onError('图标只支持 JPG 或 PNG 格式');
        return false;
    }
    if (file.size / 1024 / 1024 >= MAX_SIZE_MB) {
        onError(`图标不能超过 ${MAX_SIZE_MB} MB`);
        return false;
    }
    return true;
}

/**
 * 解析上传接口的返回值。
 * @param {object} response 后端响应
 * @returns {string|null} 成功时返回图片地址，失败返回 null
 */
export function readUploadedUrl(response) {
    if (response && response.code === 0 && response.result) {
        return response.result.url;
    }
    return null;
}
