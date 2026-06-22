/**
 * FinRag4j 前端页面测试脚本
 * 测试框架: Playwright
 * 测试内容: 各页面功能完整性和交互测试
 */

const { test, expect } = require('@playwright/test');

// 登录测试
test.describe('登录页面测试', () => {
  test('访问登录页面', async ({ page }) => {
    await page.goto('/login');
    await expect(page).toHaveTitle('FinRag4j - 登录');
    
    // 检查登录表单元素
    await expect(page.locator('input[placeholder="用户名"]')).toBeVisible();
    await expect(page.locator('input[placeholder="密码"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });

  test('成功登录系统', async ({ page }) => {
    await page.goto('/login');
    
    // 输入用户名密码
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    
    // 点击登录按钮
    await page.click('button[type="submit"]');
    
    // 验证跳转至工作台
    await expect(page).toHaveURL('/');
    await expect(page.locator('h2:has-text("工作台")')).toBeVisible();
  });

  test('错误密码登录失败', async ({ page }) => {
    await page.goto('/login');
    
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'wrongpassword');
    await page.click('button[type="submit"]');
    
    // 验证显示错误提示
    await expect(page.locator('.el-message--error')).toBeVisible();
  });
});

// 工作台测试
test.describe('工作台页面测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('工作台页面元素检查', async ({ page }) => {
    // 检查统计卡片
    await expect(page.locator('.stat-card')).toHaveCount(4);
    
    // 检查图表区域
    await expect(page.locator('.chart-container')).toBeVisible();
    
    // 检查最近文档列表
    await expect(page.locator('.recent-docs')).toBeVisible();
  });

  test('快速操作按钮测试', async ({ page }) => {
    await page.click('button:has-text("上传文档")');
    await expect(page.locator('.el-dialog:has-text("上传文档")')).toBeVisible();
    await page.click('button:has-text("取消")');
  });
});

// 知识库管理测试
test.describe('知识库管理页面测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('知识库列表页面', async ({ page }) => {
    await page.click('span:has-text("知识库管理")');
    await page.click('span:has-text("知识库列表")');
    
    await expect(page).toHaveURL('/knowledge');
    await expect(page.locator('h2:has-text("知识库管理")')).toBeVisible();
    
    // 检查表格存在
    await expect(page.locator('table')).toBeVisible();
  });

  test('创建知识库', async ({ page }) => {
    await page.click('span:has-text("知识库管理")');
    await page.click('span:has-text("知识库列表")');
    
    await page.click('button:has-text("创建知识库")');
    await expect(page.locator('.el-dialog:has-text("创建知识库")')).toBeVisible();
    
    // 填写表单
    await page.fill('input[name="name"]', '测试知识库');
    await page.fill('input[name="code"]', 'test_kb');
    await page.fill('textarea[name="description"]', '测试知识库描述');
    
    await page.click('button:has-text("保存")');
    await expect(page.locator('.el-message--success')).toBeVisible();
  });
});

// 文档管理测试
test.describe('文档管理页面测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('文档列表页面', async ({ page }) => {
    await page.click('span:has-text("知识库管理")');
    await page.click('span:has-text("文档管理")');
    
    await expect(page).toHaveURL('/documents');
    await expect(page.locator('h2:has-text("文档管理")')).toBeVisible();
    
    // 检查上传按钮
    await expect(page.locator('button:has-text("上传文档")')).toBeVisible();
  });

  test('搜索文档', async ({ page }) => {
    await page.click('span:has-text("知识库管理")');
    await page.click('span:has-text("文档管理")');
    
    await page.fill('input[placeholder="搜索文档名称"]', '测试文档');
    await page.click('button:has-icon("Search")');
    
    // 验证搜索执行
    await expect(page.locator('table')).toBeVisible();
  });
});

// RAG问答测试
test.describe('RAG问答页面测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('问答页面元素检查', async ({ page }) => {
    await page.click('span:has-text("问答中心")');
    await page.click('span:has-text("RAG问答")');
    
    await expect(page).toHaveURL('/chat');
    await expect(page.locator('h2:has-text("RAG问答")')).toBeVisible();
    
    // 检查消息列表
    await expect(page.locator('.chat-messages')).toBeVisible();
    
    // 检查输入框
    await expect(page.locator('textarea[placeholder="输入问题"]')).toBeVisible();
  });

  test('发送问题', async ({ page }) => {
    await page.click('span:has-text("问答中心")');
    await page.click('span:has-text("RAG问答")');
    
    await page.fill('textarea[placeholder="输入问题"]', '什么是信贷合规？');
    await page.click('button:has-icon("Send")');
    
    // 验证消息发送
    await expect(page.locator('.message-item.user-message')).toBeVisible();
  });
});

// 对话历史测试
test.describe('对话历史页面测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('对话历史列表', async ({ page }) => {
    await page.click('span:has-text("问答中心")');
    await page.click('span:has-text("对话历史")');
    
    await expect(page).toHaveURL('/chat-history');
    await expect(page.locator('h2:has-text("对话历史")')).toBeVisible();
    
    // 检查对话卡片
    await expect(page.locator('.history-card')).toBeVisible();
  });

  test('搜索对话', async ({ page }) => {
    await page.click('span:has-text("问答中心")');
    await page.click('span:has-text("对话历史")');
    
    await page.fill('input[placeholder="搜索对话内容"]', '测试');
    await expect(page.locator('.history-card')).toBeVisible();
  });
});

// Agent中心测试
test.describe('Agent中心页面测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('Agent中心页面', async ({ page }) => {
    await page.click('span:has-text("Agent中心")');
    
    await expect(page).toHaveURL('/agent');
    await expect(page.locator('h2:has-text("Agent中心")')).toBeVisible();
    
    // 检查功能卡片
    await expect(page.locator('.agent-card')).toHaveCount(4);
  });

  test('打开信贷材料抽取', async ({ page }) => {
    await page.click('span:has-text("Agent中心")');
    await page.click('.agent-card:has-text("信贷材料抽取")');
    
    await expect(page.locator('.el-dialog:has-text("信贷材料抽取")')).toBeVisible();
  });
});

// 用户管理测试
test.describe('用户管理页面测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('用户列表页面', async ({ page }) => {
    await page.click('span:has-text("系统管理")');
    await page.click('span:has-text("用户管理")');
    
    await expect(page).toHaveURL('/users');
    await expect(page.locator('h2:has-text("用户管理")')).toBeVisible();
    
    // 检查表格
    await expect(page.locator('table')).toBeVisible();
  });

  test('新增用户', async ({ page }) => {
    await page.click('span:has-text("系统管理")');
    await page.click('span:has-text("用户管理")');
    
    await page.click('button:has-text("新增用户")');
    await expect(page.locator('.el-dialog:has-text("新增用户")')).toBeVisible();
    
    await page.fill('input[name="username"]', 'testuser');
    await page.fill('input[name="email"]', 'test@example.com');
    await page.fill('input[name="phone"]', '13800138000');
    
    await page.click('button:has-text("保存")');
    await expect(page.locator('.el-message--success')).toBeVisible();
  });
});

// 角色管理测试
test.describe('角色管理页面测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('角色列表页面', async ({ page }) => {
    await page.click('span:has-text("系统管理")');
    await page.click('span:has-text("角色管理")');
    
    await expect(page).toHaveURL('/roles');
    await expect(page.locator('h2:has-text("角色管理")')).toBeVisible();
    
    // 检查表格
    await expect(page.locator('table')).toBeVisible();
  });

  test('配置权限', async ({ page }) => {
    await page.click('span:has-text("系统管理")');
    await page.click('span:has-text("角色管理")');
    
    await page.click('button:has-icon("Settings")');
    await expect(page.locator('.el-dialog:has-text("配置权限")')).toBeVisible();
    
    // 检查权限树
    await expect(page.locator('.el-tree')).toBeVisible();
  });
});

// 运维监控测试
test.describe('运维监控页面测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('监控页面元素检查', async ({ page }) => {
    await page.click('span:has-text("运维监控")');
    
    await expect(page).toHaveURL('/monitor');
    await expect(page.locator('h2:has-text("系统监控")')).toBeVisible();
    
    // 检查统计卡片
    await expect(page.locator('.stat-card')).toHaveCount(4);
    
    // 检查服务状态
    await expect(page.locator('.service-status')).toBeVisible();
    
    // 检查图表
    await expect(page.locator('.chart-container')).toBeVisible();
  });
});

// 页面导航测试
test.describe('页面导航测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('侧边栏导航', async ({ page }) => {
    // 测试所有菜单项导航
    const navItems = [
      { name: '工作台', url: '/' },
      { name: '知识库列表', url: '/knowledge' },
      { name: 'RAG问答', url: '/chat' },
      { name: 'Agent中心', url: '/agent' },
      { name: '工作流编排', url: '/workflow' },
      { name: '租户管理', url: '/tenant' },
      { name: '运维监控', url: '/monitor' }
    ];

    for (const item of navItems) {
      await page.click(`span:has-text("${item.name}")`);
      await expect(page).toHaveURL(item.url);
    }
  });
});

// 页面布局一致性测试
test.describe('页面布局一致性测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('所有页面具有一致的布局', async ({ page }) => {
    const pages = ['/', '/knowledge', '/documents', '/chat', '/agent', '/users', '/monitor'];
    
    for (const pageUrl of pages) {
      await page.goto(pageUrl);
      
      // 检查侧边栏存在
      await expect(page.locator('.sidebar')).toBeVisible();
      
      // 检查顶部导航
      await expect(page.locator('.top-header')).toBeVisible();
      
      // 检查面包屑
      await expect(page.locator('.el-breadcrumb')).toBeVisible();
    }
  });

  test('页面标题样式一致', async ({ page }) => {
    const pages = ['/', '/knowledge', '/chat', '/agent', '/users'];
    
    for (const pageUrl of pages) {
      await page.goto(pageUrl);
      await expect(page.locator('h2')).toHaveCSS('font-size', '24px');
      await expect(page.locator('h2')).toHaveCSS('color', 'rgb(31, 41, 55)');
    }
  });
});

// 响应式测试
test.describe('响应式布局测试', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[placeholder="用户名"]', 'admin');
    await page.fill('input[placeholder="密码"]', 'admin');
    await page.click('button[type="submit"]');
  });

  test('桌面端布局', async ({ page }) => {
    await page.setViewportSize({ width: 1920, height: 1080 });
    await page.goto('/');
    
    // 侧边栏应该显示
    await expect(page.locator('.sidebar')).toBeVisible();
    
    // 主内容应该在侧边栏右侧
    await expect(page.locator('.main-content')).toHaveCSS('margin-left', '220px');
  });
});