<template>
	<view>
		待完善
	</view>
</template>

<script>
	export default {
		data() {
			return {
				
			}
		},
		onShow() {
			const userInfo = uni.getStorageSync('userInfo');
			if (userInfo && userInfo.banned) {
				uni.showToast({
					title: '账号已被封禁',
					icon: 'none'
				});
				uni.switchTab({
					url: '/pages/me/me'
				});
				return
			}
			require('../../static/js/tabbar.js').syncTabBarSelected()
		},
		methods: {
			
		}
	}
</script>

<style>

</style>
