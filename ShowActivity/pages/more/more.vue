<template>
	<view class="container">
		<view class="page-header">
			<text class="page-title">更多</text>
			<text class="page-subtitle">更多功能入口</text>
		</view>

		<view class="menu-list">
			<view class="menu-item" @click="goDraw">
				<view class="menu-icon">签</view>
				<view class="menu-info">
					<text class="menu-name">抽签</text>
					<text class="menu-desc">创建或参与抽签</text>
				</view>
				<text class="menu-arrow">›</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		onShow() {
			const userInfo = uni.getStorageSync('userInfo')
			if (userInfo && userInfo.banned) {
				uni.showToast({
					title: '账号已被封禁',
					icon: 'none'
				})
				uni.switchTab({
					url: '/pages/me/me'
				})
				return
			}
			require('../../static/js/tabbar.js').syncTabBarSelected()
		},
		methods: {
			goDraw() {
				uni.navigateTo({
					url: '/pages/draw/draw'
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
		background-color: #f59e0b;
		color: #ffffff;
		font-size: 34rpx;
		font-weight: bold;
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
</style>
