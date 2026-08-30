<template>
	<view class="container">
		<view class="page-header">
			<text class="page-title">操作中心</text>
			<text class="page-subtitle">活动与用户管理入口</text>
		</view>

		<view v-if="isAdmin" class="menu-list">
			<view class="menu-item" @click="goActivities">
				<view class="menu-icon activity-icon">活</view>
				<view class="menu-info">
					<text class="menu-name">活动管理</text>
					<text class="menu-desc">搜索全部活动并执行管理操作</text>
				</view>
				<text class="menu-arrow">›</text>
			</view>

			<view class="menu-item" @click="goUsers">
				<view class="menu-icon user-icon">用</view>
				<view class="menu-info">
					<text class="menu-name">用户管理</text>
					<text class="menu-desc">搜索用户并进行封禁或解封</text>
				</view>
				<text class="menu-arrow">›</text>
			</view>

			<view class="menu-item" @click="goDraws">
				<view class="menu-icon draw-icon">签</view>
				<view class="menu-info">
					<text class="menu-name">抽签管理</text>
					<text class="menu-desc">按用户ID查询抽签并管理</text>
				</view>
				<text class="menu-arrow">›</text>
			</view>
		</view>

		<view v-else class="no-permission">
			<text>该页面仅限管理员访问</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				userInfo: null
			}
		},
		computed: {
			isAdmin() {
				return this.userInfo && this.userInfo.role === 'admin'
			}
		},
		onLoad() {
			this.loadUserInfo()
		},
		onShow() {
			this.loadUserInfo()
			require('../../static/js/tabbar.js').syncTabBarSelected()
		},
		methods: {
			loadUserInfo() {
				this.userInfo = uni.getStorageSync('userInfo') || null
			},
			goActivities() {
				uni.navigateTo({
					url: '/pages/operation/activities'
				})
			},
			goUsers() {
				uni.navigateTo({
					url: '/pages/operation/users'
				})
			},
			goDraws() {
				uni.navigateTo({
					url: '/pages/operation/draws'
				})
			}
		}
	}
</script>

<style>
	.container {
		min-height: 100vh;
		padding: 30rpx;
		padding-bottom: 140rpx;
		background-color: #f5f6f8;
		box-sizing: border-box;
	}

	.page-header {
		padding: 30rpx 10rpx;
	}

	.page-title {
		display: block;
		font-size: 44rpx;
		font-weight: bold;
		color: #222222;
	}

	.page-subtitle {
		display: block;
		margin-top: 12rpx;
		font-size: 28rpx;
		color: #888888;
	}

	.menu-list {
		display: flex;
		flex-direction: column;
		gap: 24rpx;
		margin-top: 30rpx;
	}

	.menu-item {
		display: flex;
		align-items: center;
		padding: 30rpx;
		background-color: #ffffff;
		border-radius: 12rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	}

	.menu-icon {
		width: 80rpx;
		height: 80rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 12rpx;
		color: #ffffff;
		font-size: 34rpx;
		font-weight: bold;
	}

	.activity-icon {
		background-color: #2f6fed;
	}

	.user-icon {
		background-color: #13b981;
	}

	.draw-icon {
		background-color: #f59e0b;
	}

	.menu-info {
		flex: 1;
		margin-left: 24rpx;
	}

	.menu-name {
		display: block;
		font-size: 32rpx;
		font-weight: bold;
		color: #333333;
	}

	.menu-desc {
		display: block;
		margin-top: 8rpx;
		font-size: 26rpx;
		color: #999999;
	}

	.menu-arrow {
		font-size: 44rpx;
		color: #cccccc;
	}

	.no-permission {
		margin-top: 100rpx;
		text-align: center;
		color: #999999;
		font-size: 28rpx;
	}
</style>
