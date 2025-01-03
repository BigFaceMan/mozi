<template>
  <div class="container mt-4">
    <h2 class="mb-4">参数配置</h2>

    <!-- 输入训练名称 -->
    <div class="form-group mb-3">
      <label for="trainingName">模型名称:</label>
      <input
        v-model="form.trainingName"
        id="trainingName"
        type="text"
        class="form-control"
        placeholder="请输入训练名称"
      />
    </div>
    <!-- 选择场景 -->
    <div class="form-group mb-3">
      <label for="pytorchVersion">选择想定</label>
      <select v-model="form.scene" id="scene" class="form-select">
        <option v-for="situation in situations" :key="situation.id" :value="situation.name">
          {{ situation.name }}
        </option>
      </select>
    </div>

    <!-- 选择模型 -->
    <div class="form-group mb-3">
      <label for="model">选择方法:</label>
      <select v-model="form.model" id="model" class="form-select">
        <option v-for="model in filteredModels" :key="model.id" :value="model.name">
          {{ model.name }}
        </option>
      </select>
    </div>

    <!-- 选择 PyTorch 版本 -->
    <!-- <div class="form-group mb-3">
      <label for="pytorchVersion">选择 PyTorch 版本:</label>
      <select v-model="form.pytorchVersion" id="pytorchVersion" class="form-select">
        <option value="1.8">1.8</option>
        <option value="1.9">1.9</option>
        <option value="1.10">1.10</option>
        <option value="2.0">2.0</option>
      </select>
    </div> -->

    <div class="form-group mb-3">
      <label for="trainingName">训练轮次:</label>
      <input
        v-model="form.trainIterations"
        id="trainingName"
        type="text"
        class="form-control"
        placeholder="5000"
      />
    </div>

    <div class="form-group mb-3">
      <label for="trainingName">学习率:</label>
      <input
        v-model="form.learningRate"
        id="trainingName"
        type="text"
        class="form-control"
        placeholder="0.001"
      />
    </div>

    <div class="form-group mb-3">
      <label for="trainingName">batch size:</label>
      <input
        v-model="form.batchSize"
        id="trainingName"
        type="text"
        class="form-control"
        placeholder="32"
      />
    </div>

    <!-- 输入模型参数配置 -->
    <!-- <div class="form-group mb-3">
      <label for="modelParams">模型参数 (key:value 格式)</label>
      <textarea
        v-model="form.modelParams"
        id="modelParams"
        class="form-control"
        rows="4"
        placeholder="输入模型参数，例如：learning_rate: 0.001, batch_size: 32"
      ></textarea>
    </div> -->

    <!-- 提交按钮 -->
    <button @click="saveConfig" class="btn btn-primary me-2">保存配置</button>
    <button @click="startTraining" class="btn btn-primary">开始训练</button>

    <!-- 输出保存的配置信息 -->
    <div v-if="showConfig" class="alert alert-success mt-3">
      <h5>保存的配置信息:</h5>
      <pre>{{ form }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useStore } from 'vuex';
import $ from 'jquery';

const store = useStore();
const models = ref([]);
const situations = ref([]);
const showConfig = ref(false);

// 从 Vuex 获取表单数据
const form = reactive({ ...store.state.train.form });

const filteredModels = computed(() => {
  return models.value.filter(models => models.situationselect == form.scene);
});

// 从后端获取模型列表
const fetchModels = () => {
  $.ajax({
    url: "http://127.0.0.1:3000/model/getlist/",
    type: "get",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      models.value = resp;
    }
  });
  $.ajax({
    url: "http://127.0.0.1:3000/situation/getlist/",
    type: "get",
    headers: {
      Authorization: "Bearer " + store.state.user.token,
    },
    success(resp) {
      situations.value = resp;
    }
  });
};

// 保存配置
const saveConfig = () => {
  store.dispatch('setForm', form);
  showConfig.value = true;
};

const startTraining = () => {
    $.ajax({
        url: "http://127.0.0.1:3000/train/add/",
        type: "post",
        headers: {
            Authorization: "Bearer " + store.state.user.token,
        },
        data: {
            trainingName: form.trainingName,
            scene: form.scene,
            model: form.model,
            pytorchVersion: form.pytorchVersion,
            modelParams: JSON.stringify(form.modelParams),
        },
        success(resp) {
            console.log(resp)
            fetchModels()
        },
        error(resp) {
            console.log(resp)
        }
    });
};
// 页面加载时获取模型列表
onMounted(fetchModels);
</script>

<style scoped>
/* 可选自定义样式 */
</style>
