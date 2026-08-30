<template>
	<view class="container">
		<view class="page-header">
			<text class="page-title">抽签</text>
			<text class="page-subtitle">创建抽签或输入邀请码参与</text>
		</view>

		<view class="menu-list">
			<view class="menu-item" @click="goCreate">
				<view class="menu-icon create-icon">创</view>
				<view class="menu-info">
					<text class="menu-name">创建抽签</text>
					<text class="menu-desc">设置内容与人数，生成邀请码</text>
				</view>
				<text class="menu-arrow">›</text>
			</view>

			<view class="menu-item" @click="goJoin">
				<view class="menu-icon join-icon">参</view>
				<view class="menu-info">
					<text class="menu-name">参与抽签</text>
					<text class="menu-desc">输入邀请码参与抽签</text>
				</view>
				<text class="menu-arrow">›</text>
			</view>

			<view class="menu-item" @click="goMy">
				<view class="menu-icon my-icon">我</view>
				<view class="menu-info">
					<text class="menu-name">我的抽签</text>
					<text class="menu-desc">查看我创建或参与的抽签</text>
				</view>
				<text class="menu-arrow">›</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		methods: {
			goCreate() {
				uni.navigateTo({
					url: '/pages/draw/create'
				})
			},
			goJoin() {
				uni.navigateTo({
					url: '/pages/draw/join'
				})
			},
			goMy() {
				uni.navigateTo({
					url: '/pages/draw/my'
				})
			}
		}
	}
</script>

<style>
	.container {
		min-height: 100vh;
		padding: 30rpx;
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

	.create-icon {
		background-color: #f59e0b;
	}

	.join-icon {
		background-color: #2f6fed;
	}

	.my-icon {
		background-color: #13b981;
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
