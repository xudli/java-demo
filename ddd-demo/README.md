# 订单系统 - DDD实现

这是一个使用领域驱动设计(DDD)实现的订单系统示例。

## DDD vs 传统设计

### 1. 设计思路对比

#### 传统设计（以数据库为中心）

- 先设计数据库表结构
- 生成实体类（JPA Entity）
- 编写Service处理业务逻辑
- 业务逻辑分散在Service层
- 实体类仅包含数据，没有行为

例如传统设计中的订单实体：

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    private String id;
    private String customerId;
    private String status;
    private BigDecimal totalAmount;
    // getters and setters
}

@Service
public class OrderService {
    public void createOrder(OrderDTO dto) {
        // 校验
        if (dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("Items cannot be empty");
        }
        
        // 创建订单
        Order order = new Order();
        order.setCustomerId(dto.getCustomerId());
        order.setStatus("PENDING");
        
        // 计算总金额
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDTO item : dto.getItems()) {
            total = total.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }
        order.setTotalAmount(total);
        
        // 保存
        orderRepository.save(order);
    }
}
```

#### DDD设计（以领域模型为中心）

- 先识别领域概念和业务规则
- 设计充血模型，将数据和行为封装在一起
- 使用值对象表达领域概念
- 通过聚合根维护业务规则
- 领域服务处理跨实体的业务逻辑

例如DDD中的订单聚合：

```java
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;
    private Money totalAmount;
    private final List<OrderItem> items;

    public void addItem(Product product, int quantity) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify confirmed order");
        }
        
        OrderItem item = new OrderItem(product, quantity);
        items.add(item);
        recalculateTotal();
    }

    public void confirm() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Order has no items");
        }
        status = OrderStatus.CONFIRMED;
    }
}
```

### 2. 主要优势

#### 2.1 更好的业务表达

- 使用领域语言，代码即文档
- 值对象表达业务概念（Money, OrderId）
- 实体方法表达业务规则
- 聚合维护不变量

#### 2.2 更强的封装性

- 数据和行为紧密结合
- 状态变更通过方法控制
- 业务规则集中在领域模型
- 防止无效状态出现

#### 2.3 更容易应对变化

- 业务规则的修改集中在领域模型
- 基础设施的变更不影响业务逻辑
- 新增功能更容易扩展
- 重构成本更低

#### 2.4 更好的可测试性

- 领域模型可独立测试
- 不依赖基础设施
- 测试更关注业务规则
- 测试代码更简洁

### 3. 项目结构

``` yaml
src/main/java/com/example/order/
├── domain/           # 领域层：核心业务逻辑
│   ├── model/       # 领域模型
│   ├── service/     # 领域服务
│   └── repository/  # 仓储接口
├── application/     # 应用层：用例编排
│   ├── service/    # 应用服务
│   └── dto/        # 数据传输对象
├── infrastructure/ # 基础设施层：技术实现
│   ├── persistence/ # 持久化相关
│   └── repository/  # 仓储实现
└── interfaces/     # 接口层：对外适配
    └── rest/       # REST接口
```

### 4. 核心模式应用

#### 4.1 值对象（Value Object）

- Money：表示金额
- OrderId：订单标识
- CustomerId：客户标识

#### 4.2 实体（Entity）

- Order：订单聚合根
- OrderItem：订单项
- Product：商品

#### 4.3 聚合（Aggregate）

- Order作为聚合根
- 管理OrderItem生命周期
- 维护业务规则

#### 4.4 仓储（Repository）

- 提供领域模型的持久化抽象
- 隐藏存储细节
- 支持领域模型的重建

### 5. 领域模型与持久化模型的映射

#### 5.1 领域模型（Domain Model）

```java
// Order聚合根
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;
    private Money totalAmount;
    private final List<OrderItem> items;

    public void addItem(Product product, int quantity) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot modify confirmed order");
        }
        OrderItem item = new OrderItem(product, quantity);
        items.add(item);
        recalculateTotal();
    }
}

// 值对象
public class CustomerId {
    private final String value;

    public CustomerId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        this.value = value;
    }
}
```

#### 5.2 持久化模型（Persistence Model）

```java
@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    private String id;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> items;
}
```

#### 5.3 模型映射（Mappers）

```java
@Mapper(componentModel = "spring", 
        uses = {OrderItemMapper.class},
        imports = {OrderId.class, CustomerId.class, Money.class})
public interface OrderMapper {
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "customerId", source = "customerId.value")
    @Mapping(target = "totalAmount", source = "totalAmount.amount")
    OrderEntity toEntity(Order order);

    @Mapping(target = "id", expression = "java(new OrderId(entity.getId()))")
    @Mapping(target = "customerId", expression = "java(new CustomerId(entity.getCustomerId()))")
    @Mapping(target = "totalAmount", expression = "java(new Money(entity.getTotalAmount()))")
    Order toDomain(OrderEntity entity);
}
```

#### 5.4 仓储实现（Repository Implementation）

```java
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final JpaOrderRepository jpaOrderRepository;
    private final OrderMapper orderMapper;

    @Override
    public Order save(Order order) {
        var entity = orderMapper.toEntity(order);
        var savedEntity = jpaOrderRepository.save(entity);
        return orderMapper.toDomain(savedEntity);
    }
}
```

### 6. 分层调用流程

```
Controller层                     应用层                      领域层                     基础设施层
OrderController ──────> OrderApplicationService ────> OrderDomainService ────> OrderRepositoryImpl
      │                         │                           │                         │
      │                         │                           │                         │
      │                         │                        Order                    OrderEntity
      │                         │                     (领域模型)                  (持久化模型)
      │                         │                           │                         │
      │                    OrderMapper <──────────────────────────────────────> JpaRepository
```

### 7. 优势说明

1. **领域模型的独立性**
   - 领域模型（Order）不包含任何持久化相关的注解
   - 可以专注于业务规则的实现
   - 便于单元测试

2. **持久化细节的封装**
   - 持久化模型（OrderEntity）处理所有数据库映射
   - JPA相关的配置和注解集中管理
   - 可以独立优化数据库访问

3. **灵活的技术选择**
   - 可以替换持久化技术而不影响业务逻辑
   - 可以使用不同的数据库而不修改领域模型
   - 支持多种数据源

4. **清晰的职责边界**
   - Controller负责处理HTTP请求
   - ApplicationService编排业务用例
   - DomainService处理领域逻辑
   - Repository处理持久化细节
