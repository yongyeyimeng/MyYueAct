<template>
	<view class="container">
		<view class="content">
			<view v-if="userInfo && userInfo.id && !showLogin">
				<view class="user-info">
					<text class="info-item">用户id：{{ userInfo.id }}</text>
					<text class="info-item">用户昵称：{{ userInfo.nickname }}</text>
					<button class="btn" v-if="!userInfo.banned" @click="showLoginForm">修改个人信息</button>
				</view>

				<view class="banned-warning" v-if="userInfo.banned">
					<text class="banned-title">账号已被封禁</text>
					<text class="banned-reason" v-if="userInfo.banReason">原因：{{ userInfo.banReason }}</text>
					<text class="banned-until">封禁至：{{ formatBanUntil(userInfo.bannedUntil) }}</text>
				</view>
				
				<!-- 用户参与的活动列表 -->
				<view class="activity-section" v-if="!userInfo.banned">
					<text class="section-title">我参与的活动</text>
					<view class="activity-list">
						<view class="activity-item" v-for="activity in joinedActivities" :key="activity.id">
							<view class="activity-header">
								<text class="activity-title">{{ activity.atName }}</text>
							</view>
							<view class="activity-content">
								<text class="activity-time">时间：{{ activity.time }}</text>
								<text class="activity-location">地点：{{ activity.location }}</text>
								<view class="activity-footer">
									<text class="activity-participants">参与人数：{{ activity.num }}</text>
									<button class="quit-btn" @click="quitActivity(activity.id)">退出</button>
								</view>
							</view>
						</view>
						
						<view v-if="joinedActivities.length === 0" class="no-activity">
							<text>暂无参与的活动</text>
						</view>
					</view>
				</view>
			</view>
			<view v-else class="login-section">
				<input class="input" type="text" v-model="nickname" placeholder="请输入昵称" />
				<input class="input" type="text" v-model="phone" placeholder="请输入手机号" />
				<button class="btn" @click="requestAuthorization">授权登录</button>
				<view v-if="userInfo" style="width:100%">
					<button class="btn" @click="cancelEdit">取消</button>
				</view>
			</view>
			<view class="contact-line" v-if="userInfo && userInfo.role !== 'admin'">
				<text>联系管理员工：18212310283</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				userInfo: null,
				nickname: '',
				phone: '',
				joinedActivities: [], // 用户参与的活动列表
				showLogin: false // 控制是否显示登录表单
			}
		},
		onLoad() {
			this.loadUserInfo();
		},
		onShow() {
			this.loadUserInfo();
			require('../../static/js/tabbar.js').syncTabBarSelected()
		},
		methods: {
			formatBanUntil(value) {
				if (!value) return '永久'
				return String(value).replace('T', ' ')
			},
			loadUserInfo() {
				const userInfo = uni.getStorageSync('userInfo');
				if (userInfo) {
					this.userInfo = userInfo;
					// 将原本的信息记录在输入框中，便于编辑时展示
					this.nickname = userInfo.nickname || '';
					this.phone = userInfo.phone || '';
					// 加载用户参与的活动
					this.loadJoinedActivities();
				}
			},
			
			showLoginForm() {
				this.showLogin = true;
				// 进入编辑模式时，预填充输入框
				if (this.userInfo) {
					this.nickname = this.userInfo.nickname || '';
					this.phone = this.userInfo.phone || '';
				}
			},

			cancelEdit() {
				this.showLogin = false;
				// 恢复信息展示
				this.loadUserInfo();
			},
			
			// 加载用户参与的活动
			loadJoinedActivities() {
				if (!this.userInfo || !this.userInfo.id) return;
				
				const API = require('../../static/js/api.js').default;
				
				uni.request({
					url: API.ACTIVITIES_JOINED,
					method: 'GET',
					data: {
						userId: this.userInfo.id
					},
					success: (res) => {
						if (res.statusCode === 200 && res.data.code === 1) {
							this.joinedActivities = res.data.data.rows || res.data.data || [];
						} else {
							uni.showToast({
								title: '加载参与的活动失败',
								icon: 'none'
							});
						}
					},
					fail: (err) => {
						console.error('加载参与的活动列表失败', err);
						uni.showToast({
							title: '网络请求失败',
							icon: 'none'
						});
					}
				});
			},
			
			// 退出活动
			quitActivity(activityId) {
				uni.showModal({
					title: '确认退出',
					content: '确定要退出这个活动吗？',
					success: (res) => {
						if (res.confirm) {
							const API = require('../../static/js/api.js').default;
							
							uni.request({
								url: API.ACTIVITIES_QUIT,
								method: 'POST',
								data: {
									activityId: activityId,
									userId: this.userInfo.id
								},
								success: (res) => {
									if (res.statusCode === 200 && res.data.code === 1) {
										uni.showToast({
											title: '退出成功',
											icon: 'success'
										});
										
										// 重新加载参与的活动列表
										this.loadJoinedActivities();
									} else {
										uni.showToast({
											title: res.data.msg || '退出失败',
											icon: 'none'
										});
									}
								},
								fail: (err) => {
									console.error('退出活动请求失败', err);
									uni.showToast({
										title: '网络请求失败',
										icon: 'none'
									});
								}
							});
						}
					}
				});
			},
			
			requestAuthorization() {
				if (!this.nickname.trim()) {
					uni.showToast({
						title: '请输入昵称',
						icon: 'none'
					});
					return;
				}
				
				if (!this.phone.trim()) {
					uni.showToast({
						title: '请输入手机号',
						icon: 'none'
					});
					return;
				}
				
				// 调用微信登录
				uni.login({
					provider: 'weixin',
					success: (loginRes) => {
						console.log("微信登录成功", loginRes);
						
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
				const API = require('../../static/js/api.js').default;
				
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
							this.userInfo = currentUser;
							this.showLogin = false;
							uni.showModal({
								title: '账号已被封禁',
								content: '封禁原因：' + (ban.reason || '未填写') + '\n封禁至：' + (ban.bannedUntil || '永久'),
								showCancel: false,
								confirmText: '我知道了'
							});
							return;
						}
						// 根据新的Result结构检查响应
						if (res.statusCode === 200 && res.data.code === 1) {
							// 保存用户信息和token
							uni.setStorageSync('token', res.data.data.token);
							uni.setStorageSync('userInfo', res.data.data.userInfo);
							
							uni.showToast({
								title: '登录成功',
								icon: 'success'
							});

							console.log('用户信息', res.data.data.userInfo);
							
							// 更新页面用户信息
							this.userInfo = res.data.data.userInfo;
							// 确保登录表单不再显示
							this.showLogin = false;
							
							// 页面跳转
							setTimeout(() => {
								// 检查是否有上一页
								const pages = getCurrentPages();
								if (pages.length > 1) {
									// 返回上一页
									uni.navigateBack();
								} else {
									// 跳转到 tabBar 页面
									uni.switchTab({
										url: '/pages/me/me'
									});
								}
							}, 1000);
						} else {
							uni.showToast({
								title: res.data.msg || '登录失败',
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
	.container {
		padding: 20rpx;
		background-color: #f5f5f5;
		min-height: 100vh;
		padding-bottom: 140rpx;
		box-sizing: border-box;
	}
	
	.content {
		margin-top: 40rpx;
	}
	
	.user-info {
		background-color: #ffffff;
		border-radius: 10rpx;
		padding: 30rpx;
		margin-bottom: 30rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
	}
	
	.info-item {
		display: block;
		font-size: 28rpx;
		color: #333;
		margin: 10rpx 0;
	}
	
	.login-section {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 40rpx;
		background-color: #fff;
		border-radius: 10rpx;
		box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.1);
	}
	
	.input {
		width: 100%;
		margin: 20rpx 0;
		padding: 20rpx;
		border: 1rpx solid #ddd;
		border-radius: 10rpx;
		font-size: 28rpx;
	}
	
	.btn {
		width: 100%;
		margin: 20rpx 0;
		background-color: #007AFF;
		color: white;
		border-radius: 10rpx;
	}
	
	.quit-btn {
		background-color: #ff4d4f;
		color: white;
		border-radius: 6rpx;
		padding: 10rpx 20rpx;
		font-size: 24rpx;
	}
	
	.section-title {
		font-size: 32rpx;
		font-weight: bold;
		margin: 40rpx 0 20rpx 0;
		display: block;
		color: #333;
	}
	
	.activity-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
	
	.activity-item {
		background-color: #ffffff;
		border-radius: 10rpx;
		padding: 20rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
	}
	
	.activity-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 15rpx;
		padding-bottom: 15rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}
	
	.activity-title {
		font-size: 32rpx;
		font-weight: bold;
		color: #333;
	}
	
	.activity-content {
		display: flex;
		flex-direction: column;
		gap: 10rpx;
	}
	
	.activity-time,
	.activity-location {
		font-size: 28rpx;
		color: #666;
		line-height: 1.5;
	}
	
	.activity-footer {
		display: flex;
		justify-content: space-between;
		margin-top: 15rpx;
		padding-top: 15rpx;
		border-top: 1rpx solid #f0f0f0;
		align-items: center;
	}
	
	.activity-participants {
		font-size: 24rpx;
		color: #999;
	}
	
	.no-activity {
		text-align: center;
		padding: 40rpx 0;
		color: #999;
	}

	.banned-warning {
		margin-top: 24rpx;
		padding: 24rpx;
		background-color: #fff2f0;
		border: 1rpx solid #ffccc7;
		border-radius: 10rpx;
	}

	.banned-title {
		display: block;
		font-size: 30rpx;
		font-weight: bold;
		color: #c0392b;
	}

	.banned-reason,
	.banned-until {
		display: block;
		margin-top: 10rpx;
		font-size: 26rpx;
		color: #c0392b;
	}

	.contact-line {
		margin-top: 40rpx;
		padding: 24rpx;
		text-align: center;
		background-color: #ffffff;
		border-radius: 10rpx;
		font-size: 26rpx;
		color: #333333;
	}
</style>
