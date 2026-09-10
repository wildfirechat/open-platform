/*
 * 复制到剪贴板。
 * navigator.clipboard 需要安全上下文（https 或 localhost），私有化部署常见的
 * http 内网地址下并不可用，所以保留 execCommand 兜底。
 */
export async function copyText(text) {
    if (!text) {
        return false;
    }
    if (navigator.clipboard && window.isSecureContext) {
        try {
            await navigator.clipboard.writeText(text);
            return true;
        } catch (e) {
            // 继续走兜底方案
        }
    }
    try {
        const area = document.createElement('textarea');
        area.value = text;
        area.setAttribute('readonly', '');
        area.style.position = 'fixed';
        area.style.opacity = '0';
        document.body.appendChild(area);
        area.select();
        const ok = document.execCommand('copy');
        document.body.removeChild(area);
        return ok;
    } catch (e) {
        return false;
    }
}
