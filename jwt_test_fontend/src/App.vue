<template>
  <header>
    <div class="wrapper">
      <router-view/>
    </div>
  </header>
</template>

<script setup>
// import { useDark, useToggle } from '@vueuse/core'
//
// useDark({
//   selector: 'html',
//   attribute: 'class',
//   valueDark: 'dark',
//   valueLight: 'light'
// })
//
// useDark({
//   onChanged(dark) { useToggle(dark) }
// })

</script>



<style scoped>
header {
  line-height: 1.5;
}
</style>
