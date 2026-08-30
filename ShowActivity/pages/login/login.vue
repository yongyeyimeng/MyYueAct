<template>
	<view>
		<button @click="wxLogin">获取微信登录</button>
	</view>
</template>

<script>
	// 引入API配置
	const API = require('../../static/js/api.js').default;
	
	export default{
		data(){
			return {
				nickname: '',
				phone: ''
			}
		},
		methods:{
			wxLogin(){
				// 显示输入框让用户输入昵称和手机号
				this.showLoginModal();
			},
			
			showLoginModal() {
				uni.showModal({
					title: '登录信息',
					content: '请输入昵称和手机号',
					editable: true,
					placeholderText: '昵称',
					success: (res) => {
						if (res.confirm) {
							this.nickname = res.content;
							// 显示输入手机号的对话框
							uni.showModal({
								title: '手机号',
								content: '请输入手机号',
								editable: true,
								placeholderText: '手机号',
								success: (res2) => {
									if (res2.confirm) {
										this.phone = res2.content;
										// 获取微信登录凭证
										this.getWechatCode();
									}
								}
							});
						}
					}
				});
			},
			
			getWechatCode() {
				uni.login({
					provider: 'weixin', //使用微信登录
					success: (loginRes) => {
						console.log("请求微信登录返回的数据", loginRes);
						// 发送登录请求到后端
						this.sendLoginRequest(loginRes.code, this.nickname, this.phone);
					},
					fail: (error) => {
						console.error("微信登录失败", error);
						uni.showToast({
							title: '微信登录失败',
							icon: 'none'
						});
					}
				});
			},
			
			// 发送登录请求到后端
			sendLoginRequest(code, nickname, phone) {
				uni.request({
					url: API.LOGIN,
					method: 'POST',
					data: {
						code: code,
						nickname: nickname,
						phone: phone
					},
					success: (res) => {
						console.log("登录请求成功", res);
						if (res.statusCode === 403 || (res.data && res.data.code === -1)) {
							const ban = res.data.data || {};
							const currentUser = uni.getStorageSync('userInfo') || {};
							currentUser.banned = true;
							currentUser.role = currentUser.role || 'user';
							currentUser.banReason = ban.reason || '';
							currentUser.bannedUntil = ban.bannedUntil || '';
							uni.setStorageSync('userInfo', currentUser);
							uni.showModal({
								title: '账号已被封禁',
								content: '封禁原因：' + (ban.reason || '未填写') + '\n封禁至：' + (ban.bannedUntil || '永久'),
								showCancel: false,
								confirmText: '我知道了',
								success: () => {
									uni.switchTab({
										url: '/pages/me/me'
									});
								}
							});
							return;
						}
							if (res.statusCode === 200 && res.data && res.data.code === 1) {
								// 保存用户信息和token（注意后端标准返回结构：{ code, msg, data }）
								const data = res.data.data || {};
								uni.setStorageSync('token', data.token);
								uni.setStorageSync('userInfo', data.userInfo);
							
							uni.showToast({
								title: '登录成功',
								icon: 'success'
							});

							console.log('用户信息', res.data.userInfo);
							
							// 跳转到首页或其他页面
							setTimeout(() => {
								// 先检查是否有上一页，如果有则返回，否则跳转到 tabBar 页面
								let pages = getCurrentPages();
								if (pages.length > 1) {
									// 返回上一页并刷新
									uni.navigateBack({
										delta: 1,
										success: () => {
											// 触发上一页的刷新事件
											let page = getCurrentPages()[getCurrentPages().length - 1];
											if (page && typeof page.onLoad === 'function') {
												page.onLoad();
											}
										}
									});
								} else {
									// 如果没有上一页，则直接跳转到 tabBar 页面
									uni.switchTab({
										url: '/pages/me/me'
									});
								}
							}, 1000);
							} else {
								uni.showToast({
									title: (res.data && (res.data.msg || res.data.message)) || '登录失败',
									icon: 'none'
								});
							}
					},
					fail: (error) => {
						console.error("登录请求失败", error);
						uni.showToast({
							title: '登录请求失败',
							icon: 'none'
						});
					}
				});
			}
		}
	}
</script>

<style>
	       
</style>
