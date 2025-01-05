import torch
import torchvision
import torchvision.transforms as transforms
from torch import nn, optim
from torch.utils.tensorboard import SummaryWriter

# 定义超参数
batch_size = 64
learning_rate = 0.001
epochs = 10

# 数据加载和预处理
transform = transforms.Compose(
    [transforms.ToTensor(),
     transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))]  # CIFAR-10的均值和标准差
)

trainset = torchvision.datasets.CIFAR10(root='./data', train=True, download=True, transform=transform)
trainloader = torch.utils.data.DataLoader(trainset, batch_size=batch_size, shuffle=True)

testset = torchvision.datasets.CIFAR10(root='./data', train=False, download=True, transform=transform)
testloader = torch.utils.data.DataLoader(testset, batch_size=batch_size, shuffle=False)

# 定义一个简单的CNN模型
class SimpleCNN(nn.Module):
    def __init__(self):
        super(SimpleCNN, self).__init__()
        self.conv1 = nn.Conv2d(3, 32, 3, padding=1)
        self.conv2 = nn.Conv2d(32, 64, 3, padding=1)
        self.fc1 = nn.Linear(64 * 8 * 8, 512)
        self.fc2 = nn.Linear(512, 10)

    def forward(self, x):
        x = torch.relu(self.conv1(x))
        x = torch.max_pool2d(x, 2)
        x = torch.relu(self.conv2(x))
        x = torch.max_pool2d(x, 2)
        x = x.view(-1, 64 * 8 * 8)  # 展平
        x = torch.relu(self.fc1(x))
        x = self.fc2(x)
        return x

# 创建模型、损失函数和优化器
model = SimpleCNN()
criterion = nn.CrossEntropyLoss()
optimizer = optim.Adam(model.parameters(), lr=learning_rate)

log_dir = f"logs/cifar10_experiment_123"
writer = SummaryWriter(log_dir=log_dir)
# 训练循环
for epoch in range(epochs):
    model.train()
    running_loss = 0.0
    correct = 0
    total = 0

    for i, (inputs, labels) in enumerate(trainloader, 0):
        optimizer.zero_grad()

        # 正向传播
        outputs = model(inputs)
        loss = criterion(outputs, labels)

        # 反向传播和优化
        loss.backward()
        optimizer.step()

        running_loss += loss.item()
        _, predicted = torch.max(outputs, 1)
        total += labels.size(0)
        correct += (predicted == labels).sum().item()

        # 每100个batch记录一次
        if i % 100 == 99:
            print(f'[Epoch {epoch + 1}, Batch {i + 1}] loss: {running_loss / 100:.3f}')
            writer.add_scalar('training loss', running_loss / 100, epoch * len(trainloader) + i)
            running_loss = 0.0

    # 记录训练集的准确率
    accuracy = 100 * correct / total
    writer.add_scalar('accuracy/train', accuracy, epoch)

    print(f'Epoch [{epoch + 1}/{epochs}], Accuracy: {accuracy:.2f}%')

    # 评估模型在测试集上的表现
    model.eval()
    correct = 0
    total = 0
    with torch.no_grad():
        for inputs, labels in testloader:
            outputs = model(inputs)
            _, predicted = torch.max(outputs, 1)
            total += labels.size(0)
            correct += (predicted == labels).sum().item()

    test_accuracy = 100 * correct / total
    writer.add_scalar('accuracy/test', test_accuracy, epoch)
    print(f'Test Accuracy: {test_accuracy:.2f}%')

print('Finished Training')

# 保存模型
torch.save(model.state_dict(), 'cifar10_cnn.pth')

# 关闭TensorBoard writer
writer.close()
