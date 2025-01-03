<template>
    <div class="container">
        <div class="row">
            <!-- User Photo -->
            <div class="col-md-3">
                <div class="card" style="margin-top: 50px;">
                    <div class="card-body">
                        <img src="../../../assets/images/user_photo.jpg" alt="User Photo" class="img-fluid">
                        <!-- Edit Button below the photo -->
                        <button type="button" class="btn btn-primary mt-3" style="margin-left: 80px;" @click="openModal">修改信息</button>
                    </div>
                </div>
            </div>
            
            <!-- User Information -->
            <div class="col-md-9">
                <div class="card" style="margin-top: 50px;">
                    <div class="card-body">
                        <h5 class="card-title">用户信息</h5>
                        <table class="table table-bordered">
                            <tbody>
                                <tr>
                                    <th scope="row">用户名</th>
                                    <td>{{ username }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">电话</th>
                                    <td>{{ phone }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">邮箱</th>
                                    <td>{{ email }}</td>
                                </tr>
                                <tr>
                                    <th scope="row">用户级别</th>
                                    <td v-if="userLevel === '0'">普通用户</td>
                                    <td v-else>管理员</td>
                                </tr>
                                <tr>
                                    <th scope="row">个性签名</th>
                                    <td>{{ signature }}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- Edit User Modal -->
        <div v-if="isModalVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="userModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="userModalLabel">编辑用户</h5>
                        <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form>
                            <!-- <div class="mb-3">
                                <label for="username" class="form-label">用户名</label>
                                <input type="text" class="form-control" id="username" v-model="form.username">
                            </div> -->
                            <div class="mb-3">
                                <label for="phone" class="form-label">电话</label>
                                <input type="text" class="form-control" id="phone" v-model="form.phone">
                            </div>
                            <div class="mb-3">
                                <label for="email" class="form-label">邮箱</label>
                                <input type="email" class="form-control" id="email" v-model="form.email">
                            </div>
                            <!-- <div class="mb-3">
                                <label for="userLevel" class="form-label">用户级别</label>
                                <select class="form-select" id="userLevel" v-model="form.userLevel">
                                    <option value="0">普通用户</option>
                                    <option value="1">管理员</option>
                                </select>
                            </div> -->
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
                        <button type="button" class="btn btn-primary" @click="saveChanges">保存更改</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>



<script>
import { useStore } from 'vuex'
import { ref, reactive } from 'vue';
import $ from 'jquery';
// import router from '../../../router/index'
export default {
    components: {
    },
    setup() {
        const store = useStore();

        let username = store.state.user.username;
        let phone = store.state.user.phone;
        let email = store.state.user.email;
        let userLevel = store.state.user.urank;
        let signature = '生活需要一点冒险';
        // Modal visibility and form data
        const isModalVisible = ref(false);
        const form = reactive({
            id: null,
            username: '',
            phone: '',
            email: '',
            userLevel: ''
        });

        // Method to open the modal
        const openModal = () => {
            isModalVisible.value = true;
        };

        // Method to close the modal
        const closeModal = () => {
            isModalVisible.value = false;
        };

        // Method to save changes (this can be further extended to update user data in a store or API)
        const saveChanges = () => {
            $.ajax({
                url: "http://127.0.0.1:3000/user/updatabyname/",
                type: "post",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                data: {
                    username: username, 
                    phone: form.phone,
                    email: form.email
                },
                success(resp) {
                    console.log(resp)
                    store.dispatch("getinfo", {
                        success() {
                            // router.push({ name: 'home' });
                            // phone = store.state.user.phone;  // 更新本地的变量
                            // email = store.state.user.email;
                            // username = store.state.user.username;
                            location.reload();
                        }
                    })

                },
                error(resp) {
                    console.log(resp)
                }
            });
            // store.commit('updateUser', form.value); // Assuming you have a mutation for updating user
            closeModal();
        };
        return {
                    username,
                    phone,
                    email,
                    userLevel,
                    signature,
                    isModalVisible,
                    form,
                    openModal,
                    closeModal,
                    saveChanges
        };
    }
}
</script>

<style scoped>
div.error-message {
    color: red;
}
</style>
