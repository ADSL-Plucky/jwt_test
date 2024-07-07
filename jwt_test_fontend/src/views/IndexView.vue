<template>
  <div>
    <el-button @click="userLogout">退出登录</el-button>
  </div>
</template>

<script setup>
import router from "@/router";
import {loginOutApi} from "@/api/welcome/welcome.js";
import {deleteAccessToken} from "@/utils/accessTokenUtils.js";

function userLogout() {
  loginOutApi().then(()=>{
    deleteAccessToken()
    ElMessage.success(`退出登录成功，欢迎您再次使用`)
    router.push("/")
  })
}
</script>

<style scoped>

</style>
