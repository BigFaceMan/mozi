<template>
    <div class="container mt-4">
        <!-- Search and Add User Button -->
        <div class="d-flex justify-content-between mb-3">
            <div class="input-group" style="width: 300px;">
                <input type="text" class="form-control" placeholder="查找用户..." v-model="searchQuery" @input="filterUsers">
                <button class="btn btn-outline-secondary" type="button" @click="resetSearch">重置</button>
            </div>
            <button class="btn btn-primary" @click="openAddUserModal">新增用户</button>
        </div>

        <!-- User Table -->
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">用户列表</h5>
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th scope="col">用户名</th>
                            <th scope="col">电话</th>
                            <th scope="col">邮箱</th>
                            <th scope="col">用户级别</th>
                            <th scope="col">操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="user in paginatedUsers" :key="user.id">
                            <td>{{ user.username }}</td>
                            <td>{{ user.phone }}</td>
                            <td>{{ user.email }}</td>
                            <!-- <td>{{ user.userLevel }}</td> -->

                            <td v-if="user.urank==='0'">普通用户</td>
                            <td v-else>管理员</td>
                            <td>
                                <button class="btn btn-sm btn-secondary" @click="openEditUserModal(user)">编辑</button>
                                <button class="btn btn-sm btn-danger ms-2" @click="deleteUser(user.id)">删除</button>
                            </td>
                        </tr>
                    </tbody>
                </table>

                <!-- Pagination Controls -->
                <nav>
                    <ul class="pagination justify-content-center">
                        <li class="page-item" :class="{ disabled: currentPage.value === 1 }">
                            <button class="page-link" @click="goToPage(currentPage - 1)">上一页</button>
                        </li>
                        <li class="page-item" :class="{ disabled: currentPage.value === totalPages }">
                            <button class="page-link" @click="goToPage(currentPage + 1)">下一页</button>
                        </li>
                    </ul>
                </nav>
            </div>
        </div>

        <!-- Add/Edit User Modal -->
        <div v-if="isModalVisible" class="modal fade show" tabindex="-1" style="display: block;" aria-labelledby="userModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 v-if="isEditing" class="modal-title" id="userModalLabel">编辑用户</h5>
                        <h5 v-else class="modal-title" id="userModalLabel">新增用户</h5>
                        <button type="button" class="btn-close" @click="closeModal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form>
                            <div class="mb-3">
                                <label for="username" class="form-label">用户名</label>
                                <input type="text" class="form-control" id="username" v-model="form.username">
                            </div>
                            <div class="mb-3">
                                <label for="phone" class="form-label">电话</label>
                                <input type="text" class="form-control" id="phone" v-model="form.phone">
                            </div>
                            <div class="mb-3">
                                <label for="email" class="form-label">邮箱</label>
                                <input type="email" class="form-control" id="email" v-model="form.email">
                            </div>
                            <div class="mb-3">
                                <label for="userLevel" class="form-label">用户级别</label>
                                <select class="form-select" id="userLevel" v-model="form.userLevel">
                                    <option value="普通用户">普通用户</option>
                                    <option value="管理员">管理员</option>
                                </select>
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>

                        <button v-if="isEditing" type="button" class="btn btn-primary" @click="saveUser">保存更改</button>
                        <button v-else type="button" class="btn btn-primary" @click="saveUser">新增用户</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue';
import $ from 'jquery';
import store from '@/store';
import { data } from 'jquery';

export default {
    setup() {
        const users = ref([]);
        const filteredUsers = ref([]);
        const paginatedUsers = ref([]);
        const searchQuery = ref('');
        const form = reactive({
            id: null,
            username: '',
            phone: '',
            email: '',
            userLevel: ''
        });
        const isEditing = ref(false);
        const currentPage = ref(1);
        const itemsPerPage = 10;
        const isModalVisible = ref(false);

        const totalPages = computed(() => Math.ceil(filteredUsers.value.length / itemsPerPage));

        const fetchUsers = () => {
            $.ajax({
                url: "http://127.0.0.1:3000/user/getlist/",
                type: "get",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                success(resp) {
                    users.value = resp;
                    filteredUsers.value = resp;
                    updatePaginatedUsers();
                }
            });
        };

        const filterUsers = () => {
            const query = searchQuery.value.trim().toLowerCase();
            filteredUsers.value = users.value.filter(user =>
                user.username.toLowerCase().includes(query)
            );
            currentPage.value = 1;
            updatePaginatedUsers();
        };

        const updatePaginatedUsers = () => {
            const start = (currentPage.value - 1) * itemsPerPage;
            const end = start + itemsPerPage;
            paginatedUsers.value = filteredUsers.value.slice(start, end);
        };

        const resetSearch = () => {
            searchQuery.value = '';
            filteredUsers.value = users.value;
            currentPage.value = 1;
            updatePaginatedUsers();
        };

        const goToPage = (page) => {
            console.log("page" + page)
            if (page > 0 && page <= totalPages.value) {
                currentPage.value = page;
                updatePaginatedUsers();
            }
        };

        const openAddUserModal = () => {
            isEditing.value = false;
            resetForm();
            isModalVisible.value = true;
        };

        const openEditUserModal = (user) => {
            isEditing.value = true;
            Object.assign(form, user);
            isModalVisible.value = true;
        };

        const resetForm = () => {
            form.id = null;
            form.username = '';
            form.phone = '';
            form.email = '';
            form.userLevel = '';
        };

        const saveUser = () => {
            if (isEditing.value) {
                updateUser();
            } else {
                addUser();
            }
        };

        const addUser = () => {
            // Add user logic here (AJAX request to add user)
            let urank = '0'
            if (form.userLevel == "普通用户") {
                urank = '0'
            }
            else {
                urank = '1'
            }
                // console.log("urank is : ", urank)
        
            $.ajax({
                url: "http://127.0.0.1:3000/user/addbyname/",
                type: "post",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                data: {
                    username: form.username,
                    urank: urank,
                    phone: form.phone,
                    email: form.email
                },
                success(resp) {
                    console.log(data);
                    console.log(resp)
                    fetchUsers();
                },
                error(resp) {
                    console.log(resp)
                }
            });

            isModalVisible.value = false;
        };

        const updateUser = () => {
            // Update user logic here (AJAX request to update user)
            let urank = '0'
            if (form.userLevel == "普通用户") {
                urank = '0'
            }
            else {
                urank = '1'
            }
                // console.log("urank is : ", urank)
        
            $.ajax({
                url: "http://127.0.0.1:3000/user/updatabyname/",
                type: "post",
                headers: {
                    Authorization: "Bearer " + store.state.user.token,
                },
                data: {
                    username: form.username,
                    urank: urank,
                    phone: form.phone,
                    email: form.email
                },
                success(resp) {
                    console.log(data);
                    console.log(resp)
                    fetchUsers();
                },
                error(resp) {
                    console.log(resp)
                }
            });

            isModalVisible.value = false;
        };

        const deleteUser = (user_id) => {
            if (confirm('确定要删除这个用户吗？')) {
                // console.log("delete : " + userid);
                $.ajax({
                    url: "http://127.0.0.1:3000/user/deletebyid/",
                    type: "post",
                    headers: {
                        Authorization: "Bearer " + store.state.user.token,
                    },
                    data: {
                        id: user_id,
                    },
                    success(resp) {
                        console.log(data);
                        console.log(resp)
                        fetchUsers();
                    },
                    error(resp) {
                        console.log(resp)
                    }
                });
            }
        };

        const closeModal = () => {
            isModalVisible.value = false;
        };

        onMounted(fetchUsers);

        return {
            users,
            filteredUsers,
            paginatedUsers,
            searchQuery,
            form,
            isEditing,
            currentPage,
            itemsPerPage,
            totalPages,
            filterUsers,
            resetSearch,
            goToPage,
            openAddUserModal,
            openEditUserModal,
            resetForm,
            saveUser,
            addUser,
            updateUser,
            deleteUser,
            updatePaginatedUsers,
            isModalVisible,
            closeModal
        };
    }
};
</script>

<style scoped>
.modal.fade.show {
    display: block;
    background: rgba(0, 0, 0, 0.5);
}
</style>
