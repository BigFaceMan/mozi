import time
import argparse
from torch.utils.tensorboard import SummaryWriter
import os
import pandas as pd
import matplotlib.pyplot as plt
from tensorboard.backend.event_processing.event_accumulator import EventAccumulator
from datetime import datetime

# 创建解析器以支持命令行参数
parser = argparse.ArgumentParser(description="Record iterations using TensorBoard.")
parser.add_argument('--tensorboardpath', type=str, required=True, help="Path to save TensorBoard logs.")
args = parser.parse_args()

# 获取当前脚本所在的目录
script_dir = os.path.dirname(os.path.abspath(__file__))

# 初始化 TensorBoard SummaryWriter
writer = SummaryWriter(log_dir=args.tensorboardpath)

# 设置总的迭代次数
total_iterations = 5

# 模拟迭代过程
for i in range(total_iterations):
    # 计算当前进度百分比
    progress_percentage = (i + 1) / total_iterations * 100

    # 输出进度
    print(f"Iteration: {i + 1}/{total_iterations}, Progress: {progress_percentage:.2f}%")

    # 记录当前的进度
    writer.add_scalar("Iteration Value", i, i)
    writer.add_scalar("Progress", progress_percentage, i)  # 记录训练进度（百分比）

    # 每 1 秒输出一次
    time.sleep(1)

# 关闭 TensorBoard writer
writer.close()

# 获取当前时间戳，确保每次输出唯一
timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')

# 获取 tensorboardpath 的基本文件夹和名称
log_base_name = os.path.basename(args.tensorboardpath)

# 创建输出目录（确保其存在）
output_dir = os.path.join(script_dir, 'outputs', f"{log_base_name}_{timestamp}")
os.makedirs(output_dir, exist_ok=True)

# 读取 TensorBoard 日志文件
event_acc = EventAccumulator(args.tensorboardpath)
event_acc.Reload()

# 提取标量数据
scalar_data = event_acc.Scalars('Iteration Value')

# 将数据转为 Pandas DataFrame
df = pd.DataFrame(scalar_data)
df['wall_time'] = pd.to_datetime(df['wall_time'], unit='s')

# 绘制训练曲线
plt.figure(figsize=(10, 6))
plt.plot(df['wall_time'], df['value'], label='Iteration Value')
plt.xlabel('Time')
plt.ylabel('Iteration Value')
plt.title('Training Progress')
plt.legend()
plt.grid(True)

# 生成唯一的图片文件名
image_filename = f"{log_base_name}_{timestamp}_training_progress.png"
image_path = os.path.join(output_dir, image_filename)

# 保存训练曲线为图片
plt.savefig(image_path)
plt.close()

# 生成 HTML 报告
html_content = f"""
<html>
<head>
    <title>Training Report</title>
</head>
<body>
    <h1>Training Progress Report</h1>
    <h2>Iteration Value vs Time</h2>
    <!-- 使用相对路径来引用图片 -->
    <img src="{image_filename}" alt="Training Progress">
</body>
</html>
"""

# 生成唯一的 HTML 报告文件名
html_filename = f"{log_base_name}_{timestamp}_training_report.html"
html_report_path = os.path.join(output_dir, html_filename)

# 使用 'utf-8' 编码保存 HTML 报告
with open(html_report_path, 'w', encoding='utf-8') as f:
    f.write(html_content)

print(f"Training report has been saved as {html_report_path}.")
