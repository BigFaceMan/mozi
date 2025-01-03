package org.example.backend.service.impl.pk;

import java.io.File;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.example.backend.mapper.LogmessageMapper;
import org.example.backend.mapper.TrainLogMapper;
import org.example.backend.mapper.TrainMapper;
import org.example.backend.pojo.Model;
import org.example.backend.pojo.Train;
import org.example.backend.pojo.TrainLog;
import org.example.backend.pojo.User;
import org.example.backend.service.impl.utils.UserDetailsImpl;
import org.example.backend.service.pk.ModelTrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ModelTrainingServiceImpl implements ModelTrainingService {

    @Autowired
    private TrainMapper trainMapper;

    @Autowired
    private TrainLogMapper trainLogMapper;
    @Autowired
    private LogmessageMapper logmessageMapper;
    private Process tensorBoardProcess = null;

    private static final ConcurrentHashMap<String, Thread> runningThreads = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> addTrain(MultiValueMap<String, String> data) {
        String trainingName_pre = data.getFirst("trainingName");
        long currentTimeMillis = System.currentTimeMillis(); // 获取当前的时间戳
        Date currentDate = new Date(currentTimeMillis); // 将时间戳转换为Date对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // 定义日期格式
        String formattedDate = dateFormat.format(currentDate); // 格式化日期
        String trainingName = trainingName_pre + "_" + formattedDate;
        String model = data.getFirst("model");
        String pytorchVersion = data.getFirst("pytorchVersion");
        String modelParams = data.getFirst("modelParams");
        String scene = data.getFirst("scene");
//        Integer scene = Integer.parseInt(data.getFirst("scene"));
        String projectPath = System.getProperty("user.dir") + "\\src\\main\\python\\";
        String checkpointpath = projectPath + File.separator + "checkpoint\\" + trainingName + "_" + scene + ".pth";
        String tensorboardpath = projectPath + File.separator + "logs\\" + trainingName + "_" + scene;

        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();

        Map<String, String> result = new HashMap<>();
        Train train = new Train(null, trainingName, pytorchVersion, scene, model, modelParams, checkpointpath, 1, tensorboardpath, user.getId());
        trainMapper.insert(train);
        putTrain(data);
        result.put( "error_message", "success");
        return result;
    }
    @Override
    public Map<String, String> addTensorboard(MultiValueMap<String, String> data) {
        Map<String, String> result = new HashMap<>();
        String tensorboardpath = data.getFirst("tensorboardpath");

        // 启动一个线程来执行 TensorBoard 命令
        new Thread(() -> {
            try {
                // 构建 TensorBoard 命令
                String[] command = {
                        "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                        "conda activate ssp && tensorboard --logdir=" + "C:\\Users\\Lenovo\\Desktop\\research\\kaifa\\demo"
                };

                // 使用 ProcessBuilder 启动 TensorBoard
                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出

                Process tensorBoardProcess = processBuilder.start(); // 启动进程
                int exitCode = tensorBoardProcess.waitFor(); // 等待进程结束并获取退出码

                if (exitCode != 0) {
                    // 如果退出码不为 0，表示启动失败
                    result.put("error_message", "failed");
                } else {
                    // 正常启动
                    result.put("error_message", "success");
                }

            } catch (IOException e) {
                // 捕获启动时的异常
                e.printStackTrace();
                result.put("error_message", "failed: IOException occurred");
            } catch (InterruptedException e) {
                // 捕获线程中断异常
                e.printStackTrace();
                result.put("error_message", "failed: Process was interrupted");
            }
        }).start();

        // 默认返回 "success"（但启动结果可能需要异步检查）
        result.put("error_message", "success");
        return result;
    }

    // killLog函数，终止 TensorBoard 进程
    public Map<String, String> deleteTensorboard() {
        Map<String, String> result = new HashMap<>();
        if (tensorBoardProcess != null) {
            tensorBoardProcess.destroy();  // 销毁进程
            result.put("error_message", "TensorBoard process killed successfully");
        } else {
            result.put("error_message", "No running TensorBoard process found");
        }
        return result;
    }
    @Override
    public void putTrain(MultiValueMap<String, String> data) {
        String trainingName_pre = data.getFirst("trainingName");
        long currentTimeMillis = System.currentTimeMillis(); // 获取当前的时间戳
        Date currentDate = new Date(currentTimeMillis); // 将时间戳转换为Date对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss"); // 定义日期格式
        String formattedDate = dateFormat.format(currentDate); // 格式化日期
        String trainingName = trainingName_pre + "_" + formattedDate;
        String model = data.getFirst("model");
        String pytorchVersion = data.getFirst("pytorchVersion");
        String modelParams = data.getFirst("modelParams");
        String scene = data.getFirst("scene");
        String projectPath = System.getProperty("user.dir") + "\\src\\main\\python\\";
        String checkpointpath = projectPath + File.separator + "checkpoint\\" + trainingName + "_" + scene + ".pth";
        String tensorboardpath = projectPath + File.separator + "logs\\" + trainingName + "_" + scene;
        Thread trainingThread = new Thread(() -> {
            Process trainingProcess = null;
            boolean isInterrupted = false; // 中断标志
            try {
                String[] command = {
                        "cmd.exe", "/c", // Windows 下需要使用 cmd.exe
                        "conda activate ssp && python C:\\Users\\Lenovo\\Desktop\\research\\kaifa\\202411\\backend\\src\\main\\python\\test.py --tensorboardpath " + tensorboardpath
                };

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true); // 合并标准输出和错误输出
                trainingProcess = processBuilder.start();

                // 等待进程执行完毕，检查中断状态
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (trainingProcess.waitFor(1, TimeUnit.SECONDS)) {
                            break; // 进程正常结束，退出
                        }
                    } catch (InterruptedException e) {
                        // 捕获到 InterruptedException 后，退出等待并销毁进程
                        Thread.currentThread().interrupt();  // 恢复中断状态
                        trainingProcess.destroy();  // 销毁进程
                        System.out.println("线程被中断，进程已被销毁");
                        isInterrupted = true; // 设置标志为 true
                        return;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // 更新数据库状态
                if (!isInterrupted) {
                    UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("trainingname", trainingName).set("running", 0);
                    boolean isUpdated = trainMapper.update(null, updateWrapper) > 0;
                    if (isUpdated) {
                        System.out.println("成功更新训练进程状态");
                    } else {
                        System.out.println("未成功更新训练进程状态");
                    }
                } else {
                    System.out.println("线程被中断，跳过数据库更新");
                }
            }
        });
        // 保存线程引用到Map中
        runningThreads.put(trainingName, trainingThread);
        // 启动线程
        trainingThread.start();

    }
    @Override
    public Map<String, String> stopTrain(MultiValueMap<String, String> data) {
        String trainingName = data.getFirst("trainingName");
        System.out.println("trainingName");
        System.out.println(trainingName);
        System.out.println("Threads : ");
        System.out.println(runningThreads.size());
        Thread trainingThread = runningThreads.get(trainingName);

        Map<String, String> result = new HashMap<>();
        if (trainingThread != null) {
            // 需要实现线程终止的逻辑，例如通过发送中断信号
            trainingThread.interrupt();
            runningThreads.remove(trainingName);
            System.out.println("训练已终止: " + trainingName);
            UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("trainingname", trainingName).set("running", 2);
            boolean isUpdated = trainMapper.update(null, updateWrapper) > 0;
            if (isUpdated) {
                System.out.println("成功更新训练进程状态");
                result.put( "error_message", "成功更新训练进程状态");
            } else {
                System.out.println("未成功更新训练进程状态");
                result.put( "error_message", "未成功更新训练进程状态");
            }
        } else {
            System.out.println("未找到训练任务: " + trainingName);
        }
        return result;
    }

    @Override
    public Map<String, String> continueTrain(MultiValueMap<String, String> data) {
        Map<String, String> result = new HashMap<>();
        String trainingName = data.getFirst("trainingName");
        UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("trainingname", trainingName).set("running", 1);
        boolean isUpdated = trainMapper.update(null, updateWrapper) > 0;
        if (isUpdated) {
            System.out.println("成功更新训练进程状态");
            result.put( "error_message", "成功更新训练进程状态");
        } else {
            System.out.println("未成功更新训练进程状态");
            result.put( "error_message", "未成功更新训练进程状态");
        }
        return result;
    }

    @Override
    public Map<String, String> killTrain(MultiValueMap<String, String> data) {
        Map<String, String> result = new HashMap<>();
        String trainingName = data.getFirst("trainingName");
        UpdateWrapper<Train> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("trainingname", trainingName).set("running", 0);
        boolean isUpdated = trainMapper.update(null, updateWrapper) > 0;
        if (isUpdated) {
            System.out.println("成功更新训练进程状态");
            result.put( "error_message", "成功更新训练进程状态");
        } else {
            System.out.println("未成功更新训练进程状态");
            result.put( "error_message", "未成功更新训练进程状态");
        }
        return result;
    }
    @Override
    public Map<String, String> addTrainLog(MultiValueMap<String, String> data) {
        Map<String, String> result = new HashMap<>();
        TrainLog trainLog = new TrainLog();
        trainLog.setTrainingname(data.getFirst("trainingname"));
        trainLog.setProgress(Integer.parseInt(data.getFirst("progress")));
        trainLog.setLoss(Float.parseFloat(data.getFirst("loss")));
        trainLog.setCpuUsage(Float.parseFloat(data.getFirst("cpu_usage")));
        trainLog.setGpuUsage(Float.parseFloat(data.getFirst("gpu_usage")));
        trainLog.setMemoryUsage(Float.parseFloat(data.getFirst("memory_usage")));
        trainLog.setVramUsage(Float.parseFloat(data.getFirst("vram_usage")));
        trainLog.setNetworkBandwidth(Float.parseFloat(data.getFirst("network_bandwidth")));

        Date now = new Date();
        trainLog.setTimestamp(now);

        int rows = trainLogMapper.insert(trainLog);
        result.put("status", rows > 0 ? "success" : "failure");
        return result;
    }

    @Override
    public List<Train> getList() {
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        UserDetailsImpl loginUser = (UserDetailsImpl) authentication.getPrincipal();
        User user = loginUser.getUser();
        List<Train> trainList;
        if (user.getUrank().equals("0")) {
            System.out.println("根据当前uid来查找对应的模型信息");
            QueryWrapper<Train> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("uid", user.getId());  // 过滤出 uid 等于 user.getId() 的记录
            trainList = trainMapper.selectList(queryWrapper);
        } else {
            trainList = trainMapper.selectList(null);
        }
        Collections.reverse(trainList);  // 反转列表
        return trainList;
    }

    @Override
    public List<TrainLog> getLogList(Map<String, String> data) {
        String trainingname = data.get("trainingname");

        QueryWrapper<TrainLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("trainingname", trainingname)  // 根据 trainingname 筛选
                .orderByAsc("timestamp");         // 按 timestamp 从小到大排序

        return trainLogMapper.selectList(queryWrapper);
    }

    @Override
    public Map<String, String> removeTrain(Map<String, String> data) {
        Map<String, String> result = new HashMap<>();
        int id = Integer.parseInt(data.get("id"));

        int rows = trainMapper.deleteById(id);
        result.put("status", rows > 0 ? "success" : "failure");
        return result;
    }

    @Override
    public Map<String, String> removeTrainLog(Map<String, String> data) {
        Map<String, String> result = new HashMap<>();
        int id = Integer.parseInt(data.get("id"));

        int rows = trainLogMapper.deleteById(id);
        result.put("status", rows > 0 ? "success" : "failure");
        return result;
    }
}
