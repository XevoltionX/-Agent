import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('@/views/Home.vue'), meta: { public: true } },
  { path: '/product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue'), meta: { public: true } },
  { path: '/merchant/login', name: 'MerchantLogin', component: () => import('@/views/MerchantLogin.vue'), meta: { public: true } },
  { path: '/merchant/dashboard', name: 'MerchantDashboard', component: () => import('@/views/MerchantDashboard.vue'), meta: { auth: true } },
  { path: '/merchant/profile', name: 'MerchantProfile', component: () => import('@/views/MerchantProfile.vue'), meta: { auth: true } },
  { path: '/merchant/publish', name: 'PublishProduct1', component: () => import('@/views/PublishProduct1.vue'), meta: { auth: true } },
  { path: '/merchant/publish/ai', name: 'AiGenerateResult', component: () => import('@/views/AiGenerateResult.vue'), meta: { auth: true } },
  { path: '/merchant/publish/edit', name: 'PublishProduct2', component: () => import('@/views/PublishProduct2.vue'), meta: { auth: true } },
  { path: '/merchant/publish/manage/:id', name: 'PublishProduct3', component: () => import('@/views/PublishProduct3.vue'), meta: { auth: true } },
  { path: '/merchant/products', name: 'ProductManage', component: () => import('@/views/ProductManage.vue'), meta: { auth: true } },
  { path: '/merchant/leads', name: 'LeadList', component: () => import('@/views/LeadList.vue'), meta: { auth: true } },
  { path: '/merchant/leads/:id', name: 'LeadDetail', component: () => import('@/views/LeadDetail.vue'), meta: { auth: true } },
  { path: '/merchant/account', name: 'AccountPermissions', component: () => import('@/views/AccountPermissions.vue'), meta: { auth: true } },
  { path: '/merchant/notifications', name: 'Notifications', component: () => import('@/views/Notifications.vue'), meta: { auth: true } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() { return { top: 0 } }
})

// 导航守卫：检查登录态
router.beforeEach((to, from, next) => {
  if (to.meta.auth) {
    const token = localStorage.getItem('gaocui_token')
    if (!token) {
      return next({ name: 'MerchantLogin', query: { redirect: to.fullPath } })
    }
  }
  // 已登录用户访问登录页时重定向到后台
  if (to.name === 'MerchantLogin') {
    const token = localStorage.getItem('gaocui_token')
    if (token) {
      return next({ name: 'MerchantDashboard' })
    }
  }
  next()
})

export default router
