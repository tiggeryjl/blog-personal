/* eslint-env node */
require('@rushstack/eslint-patch/modern-module-resolution')

module.exports = {
  root: true,
  extends: [
    'plugin:vue/vue3-essential',
    'eslint:recommended',
    '@vue/eslint-config-prettier'
  ],
  parserOptions: {
    ecmaVersion: 'latest'
  },
  rules: {
    'prettier/prettier': 'off',
    // 🔥 关键：关闭组件名多词检查
    'vue/multi-word-component-names': 'off',
    // 或者改为警告
    // 'vue/multi-word-component-names': 'warn',

    'no-unused-vars': 'warn'
  }
}
