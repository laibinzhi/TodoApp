## 前言

在空闲的时候，就要写代码来巩固以下自己的知识体系。所以呢，使用Room和WorkManager在Android架构组件下，实现一个查看Task列表，左滑右滑删除item，新建附带提醒功能的Task的App。

本文会牵涉以下知识点

- Android架构组件
- Jetpack - Room
- Jetpack - WorkManager
- Kotlin Coroutines
- Recyclerview 自定义左滑右滑事件的实现

本文会从系统架构到详细代码，一步一步进行介绍，敬请期待...

## 截图

<html>
 <img src="http://lbz-blog.test.upcdn.net/post/todoapp_empty.jpg" width = "180" height = "390"  border="1"  />
</html>

<html>
 <img src="http://lbz-blog.test.upcdn.net/post/todoapp-list.jpg" width = "180" height = "390"  border="1"  />
</html>

<html>
 <img src="http://lbz-blog.test.upcdn.net/post/todoapp-newtask.jpg" width = "180" height = "390"  border="1"  />
</html>



<html>
 <img src="http://lbz-blog.test.upcdn.net/post/todoapp-day-select.jpg" width = "180" height = "390"  border="1"  />
</html>

<html>
 <img src="http://lbz-blog.test.upcdn.net/post/todoapp-time-select.jpg" width = "180" height = "390"  border="1"  />
</html>

<html>
 <img src="http://lbz-blog.test.upcdn.net/post/todoapp-notification.jpg" width = "180" height = "390"  border="1"  />
</html>

<html>
 <img src="http://lbz-blog.test.upcdn.net/post/todoapp-delete.gif" width = "180" height = "390"  border="1"  />
</html>


## 架构组件

下图为我们的系统架构组件图，为Google推荐的一种实现

![image](http://lbz-blog.test.upcdn.net/post/a7da8f5ea91bac52.png)

下面来解释一下

- [Entity](https://developer.android.com/reference/androidx/room/Entity)： 实体类，带注释的类，在Room中充当与数据库的一个表
- SQLite：使用封装好了的Room充当持久性库，创建并维护此数据库
- [Dao](https://developer.android.com/reference/androidx/room/Dao.html)： 数据访问对象。SQL查询到该函数的映射，使用DAO时，您将调用方法，而Room负责其余的工作。
- [Room数据库](https://developer.android.com/topic/libraries/architecture/room) ：底层还是SQLite的实现，数据库使用DAO向SQLite数据库发出查询。
- Repository：存储库，主要用于管理多个数据源，通常充作ViewModel和数据获取的桥梁。
- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)：充当存储库（数据）和UI之间的通信中心。UI不再需要担心数据的来源。ViewModel不会因为activity或者fragment的生命周期而丢失。
- [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)：以观察到的数据持有者类。始终保存/缓存最新版本的数据，并在数据更改时通知其观察者。LiveData知道生命周期。UI组件仅观察相关数据，而不会停止或继续观察。LiveData自动管理所有这些，因为它在观察的同时知道相关生命周期状态的变化。



下面是TodoApp的系统框架图
![image](http://lbz-blog.test.upcdn.net/post/TodoApp.png)

每个封闭框（SQLite数据库除外）都代表我们将创建的每一个类

## 创建程序

1. 打开Android Studio，然后单击**Start a new Android Studio *project***
2. 在“创建新项目”窗口中，选择**Empty *Activity*** ，然后单击***Next***。
3. 在下一个界面，将应用命名为TodoApp，然后点击***Finish***。

## 更新Gradle文件
1. 打开**build.gradle (Moudle：app)**
2. 在顶部使用**kapt**注释处理器和kotlin的ext函数

```
apply plugin: 'kotlin-kapt'
apply plugin: 'kotlin-android-extensions'
```

3. 在android节点添加packagingOptions，防止出现警告

```
android {
    packagingOptions {
        exclude 'META-INF/atomicfu.kotlin_module'
    }
}
```

4. 在代码dependencies块的末尾添加以下代码


```
 // Room components
    implementation "androidx.room:room-runtime:$rootProject.roomVersion"
    kapt "androidx.room:room-compiler:$rootProject.roomVersion"
    implementation "androidx.room:room-ktx:$rootProject.roomVersion"
    androidTestImplementation "androidx.room:room-testing:$rootProject.roomVersion"

    // Lifecycle components
    implementation "androidx.lifecycle:lifecycle-extensions:$rootProject.archLifecycleVersion"
    kapt "androidx.lifecycle:lifecycle-compiler:$rootProject.archLifecycleVersion"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$rootProject.archLifecycleVersion"

    // Kotlin components
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
    api "org.jetbrains.kotlinx:kotlinx-coroutines-core:$rootProject.coroutines"
    api "org.jetbrains.kotlinx:kotlinx-coroutines-android:$rootProject.coroutines"

    // Material design
    implementation "com.google.android.material:material:$rootProject.materialVersion"

    // Testing
    testImplementation 'junit:junit:4.12'
    androidTestImplementation "androidx.arch.core:core-testing:$rootProject.coreTestingVersion"

    implementation 'com.amulyakhare:com.amulyakhare.textdrawable:1.0.1'

    //workManager
    def work_version = "2.3.4"
    implementation "androidx.work:work-runtime-ktx:$work_version"

    //在电脑上用浏览器调试room，能够可视化增删改查
    def version_debug_database = "1.0.6"
    debugImplementation "com.amitshekhar.android:debug-db:$version_debug_database"
    debugImplementation "com.amitshekhar.android:debug-db-encrypt:$version_debug_database"
```

5. 打开**build.gradle (Project：TodoApp)**,在最末未添加以下代码


```
ext {
    roomVersion = '2.2.5'
    archLifecycleVersion = '2.2.0'
    coreTestingVersion = '2.1.0'
    materialVersion = '1.1.0'
    coroutines = '1.3.4'
}
```

## 创建实体类

我们的实体类是Task,任务，我们需要哪些字段呢？

首先，我们肯定需要任务的名称name，然后需要任务的描述desc，然后我们用一个boolean来标志是否需要提醒，同时，用Date日期类记录提醒时间，然后，我们需要一个界面Image的颜色color，最后，我们需要一个每一个任务对应的workmanager_id,这个id主要是删除item的时候，WorkManager结束任务用的，这个后面再细说，此处不作过多描述。


```
@Entity(tableName = "task_table")
@TypeConverters(DateConverter::class)
data class Task(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "desc")
    val desc: String,
    @ColumnInfo(name = "time")
    val time: Date?,
    @ColumnInfo(name = "hasReminder")
    val hasReminder: Boolean,//是否有提醒
    @ColumnInfo(name = "color")
    val color: Int
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
    @ColumnInfo(name = "work_manager_uuid")
    var work_manager_uuid: String = ""
}
```

我们来看看这些注解的作用

- @Entity(tableName = "task_table")

每个@Entity类代表一个SQLite表。注释您的类声明以表明它是一个Entity。如果希望表名与类名不同，则可以指定表名，例如命名为“task_table”。

- @PrimaryKey

每个实体都需要一个主键。我们设定一个Long值作为主键，初始值为0，并让他自增长（autoGenerate = true）

- @ColumnInfo(name = "name")

如果希望表中的列名与成员变量的名称不同，则指定该列名。这将列命名为name。

- TypeConverters

因为Room数据库只能保存基础类型（Int,String,Boolean,Float等），对于一些obj，则需要转换，我们定义了一个**DateConverter**转换，保存数据库的时候，把Date转成long，取值的时候，再把Long转成Date。代码如下

```
class DateConverter {

    @TypeConverter
    fun revertDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun converterDate(date: Date?): Long? {
        return date?.time
    }

}
```

## 创建Dao

#### 什么是Dao？
Dao是数据库访问对象，指定SQL查询语句和它调用的方法关联，例如Query,Insert,Delete,Update等。

DAO必须是**接口**或**抽象类**

Room可以使用协程，在方法名前面加**suspend**修饰符

#### 怎么使用Dao?

我们接下来就编写一个Dao，来实现对Task增删改查。代码如下


```
@Dao
interface TaskDao {

    @Query("SELECT * from task_table")
    fun getAllTask(): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(task: Task)

    @Query("DELETE FROM task_table")
    fun deleteAll()

    @Delete
    fun remove(task: Task)

}
```

我们看一下上面的代码的一些解说

- TaskDao是一个接口；因为我们上面提过DAO必须是接口或抽象类。
- 用@Dao来标志这个接口是作为Room的Dao
- insert(task: Task)，声明插入一个新Task的方法
- @Insert，插入执行，无须写SQL语句，同样无须写SQL语句的还有Delete,Update
- onConflict = OnConflictStrategy.IGNORE：如果所选的onConflict策略与列表中已有的Task完全相同，则会忽略该Task
- fun deleteAll()声明一个删除所有Task的方法
- remove(task: Task)声明一个删除单个Task的方法
- fun getAllTask(): LiveData<List<Task>> 一个返回LiveData包含所有Task的集合对象，外部通过监听这个对象，实现布局的刷新...
- @Query("SELECT * from task_table ")：查询返回所有Task列表，可以拓展插入一些升序降序或者过滤的查询语句

## LiveData

数据更改时，通常需要采取一些措施，例如在UI中显示更新的数据。这意味着您必须观察数据，以便在数据更改时可以做出反应。

根据数据的存储方式，这可能很棘手。观察应用程序多个组件之间的数据更改可以在组件之间创建明确的，严格的依赖路径。这使测试和调试变得非常困难。

**[LiveData](https://developer.android.com/topic/libraries/architecture/livedata.html)**，用于数据观察的生命周期库类可解决此问题。LiveData在方法描述中使用类型的返回值，然后Room会生成所有必要的代码来更新LiveData数据库。

在TaskDao中，返回LiveData包含所有Task的集合对象，然后后面的**MainActivity**我们监听它

```
@Query("SELECT * from task_table")
fun getAllTask(): LiveData<List<Task>>
```

## Room database

#### 什么是Room database

- Room是SQLite数据库的顶层调用。
- Room的工作任务类似于以前SQlite的[SQLiteOpenHelper](https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html)
- Room使用DAO向其数据库增删改查操作
- Room的SQL语句在编译中会检查该语法

#### 怎么使用Room database

Room数据库类必须是抽象类，并且是继承自**RoomDatabase**，一般是以单例模式的方式存在。

现在我们就来构建一个**TaskRoomDatabase**，代码如下

```
@Database(entities = [Task::class], version = 1)
abstract class TaskRoomDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): TaskRoomDatabase {
            // 如果INSTANCE为null，返回此INSTANCE，否则，创建database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskRoomDatabase::class.java,
                    "task_database"
                )
                    // 如果没有迁移数据库，则擦除并重建而不是迁移。
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```
我们看一下以上代码
- 使用@Database注解，标明这个类是数据库类，然后指定它的实体类(可以设置多个)还有版本号。
- TaskRoomDatabase 通过它的抽象对象TaskDao获取对象进行操作
- 数据库一般是单例模式，防止同时打开多个数据库实例


## 储存库Repository


![image](http://lbz-blog.test.upcdn.net/post/respotory.png)

在最常见的示例中，存储库实现了用于确定是从网络中获取数据还是使用本地数据库中缓存的结果的逻辑。

TaskRepository的实现如下


```
// 在构造器中声明Dao的私有属性，通过Dao而不是整个数据库，因为只需要访问Dao
class TaskRepository(private val taskDao: TaskDao) {

    // Room在单独的线程上执行所有查询
    // 观察到的LiveData将在数据更改时通知观察者。
    val allWords: LiveData<List<Task>> = taskDao.getAllTask()

    fun insert(task: Task) {
        taskDao.insert(task)
    }

    fun remove(task: Task) {
        taskDao.remove(task)
    }
}
```

注意，
- DAO作为TaskRepository的构造函数，无须用到数据库实例，安全。
- 通过LiveData从Room 获取Task列表进行初始化。Room在单独的线程上执行查询Task操作，LiveData当数据更改时，观察者将在主线程上通知观察者。
- 存储库旨在在不同的数据源之间进行中介。在这个TodoApp中,只有Room一个数据源，因此存储库不会做很多事情。有关更复杂的实现，可以看我写的一个[例子](https://github.com/laibinzhi/GoogleArchitecture)

## ViewModel
#### 什么是什么是ViewModel？

[ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)提供数据给UI,能在activity和fragment周期改变的时候保存。一般是连接Repository和Activity/Fragment的中间枢纽，还可以使用它共享数据。

![image](http://lbz-blog.test.upcdn.net/post/viewmodel.png)

ViewModel把数据和UI分开，可以更好地遵循单一职责原则。

一般ViewModel会搭配LiveData一起使用，LiveData搭配ViewModel的好处有很多：

- 将观察者放在数据上（不用轮询更改），并且仅在数据实际更改时才更新UI。
- ViewModel分割了储存库和UI
- 更高可测试性

#### viewModelScope

在Kotlin，所有协程都在内运行CoroutineScope。scope通过job来控制协程的生命周期.，当scope中的job取消时，它也会一起取消在该scope作用域范围内启动的所有协程。

AndroidX *lifecycle-viewmodel-ktx*库添加了viewModelScope类的扩展功能ViewModel，可以在其作用域下进行工作

下面，看一下TaskViewModel的实现


```
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository

    // 使用LiveData并缓存getAllTask返回的内容有几个好处：
    // - 每当Room数据库有更新的时候通知观察者，而不是轮询更新
    //   数据变化适时更新UI。
    // - 存储库通过ViewModel与UI完全隔离。
    val allWords: LiveData<List<Task>>

    init {
        val taskDao = TaskRoomDatabase.getDatabase(application, viewModelScope).taskDao()
        repository = TaskRepository(taskDao)
        allWords = repository.allWords
    }

    /**
     * 启动新的协程以非阻塞方式插入数据
     */
    fun insert(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        try {
            repository.insert(task)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun remove(task: Task) = viewModelScope.launch(Dispatchers.IO) {
        try {
            repository.remove(task)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
```


我们使用 viewModelScope.launch(Dispatchers.IO)这协程方法操作数据库。避免了主线程被阻塞。


## Task列表xml布局
1. 首先添加task item的布局信息***task_list_item*.xml**


```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/listItemLinearLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="1dp"
    android:background="@android:color/white"
    android:gravity="center"
    android:orientation="horizontal">

    <ImageView
        android:id="@+id/toDoListItemColorImageView"
        android:layout_width="45dp"
        android:layout_height="45dp"
        android:layout_marginLeft="16dp"
        android:gravity="center" />


    <RelativeLayout
        android:layout_width="0dp"
        android:layout_height="?android:attr/listPreferredItemHeight"
        android:layout_marginLeft="16dp"
        android:layout_weight="5"
        android:gravity="center">

        <TextView
            android:id="@+id/toDoListItemTextview"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_alignParentTop="true"
            android:ellipsize="end"
            android:gravity="start|bottom"
            android:lines="1"
            android:text="Clean your room"
            android:textColor="@color/secondary_text"
            android:textSize="16sp"
            tools:ignore="MissingPrefix" />

        <TextView
            android:id="@+id/todoListItemTimeTextView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_below="@id/toDoListItemTextview"
            android:gravity="start|center"
            android:text="27 Sept 2015, 22:30"
            android:textColor="?attr/colorAccent"
            android:textSize="12sp" />
    </RelativeLayout>

</LinearLayout>

```

然后在MainActivity中的布局***activity_main*.xml**，加入RecyclerView,和空布局toDoEmptyView，另外还有一个fab按钮，点击进入AddTaskActivity新建Task


```
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="#F0F1F9"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        tools:listitem="@layout/task_list_item" />

    <LinearLayout
        android:id="@+id/toDoEmptyView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:orientation="vertical"
        android:visibility="gone"
        tools:visibility="gone">

        <ImageView
            android:layout_width="100dp"
            android:layout_height="100dp"
            android:src="@drawable/empty_view_bg" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center"
            android:paddingTop="4dp"
            android:paddingBottom="8dp"
            android:text="@string/no_todo_data"
            android:textColor="@color/secondary_text"
            android:textSize="16sp" />

    </LinearLayout>


    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:contentDescription="@string/add_task"
        android:src="@drawable/ic_baseline_add_24"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintRight_toRightOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>


```

## RecyclerView和Adapter


```
class TaskListAdapter internal constructor(
    private val context: Context
) : RecyclerView.Adapter<TaskListAdapter.ViewHolder>(),
    ItemTouchHelperClass.ItemTouchHelperAdapter {

    interface OnItemEventListener {
        fun onItemRemoved(task: Task)
        fun onItemClick(task: Task)
    }

    fun setOnItemEventListener(listener: OnItemEventListener) {
        this.listener = listener
    }

    private lateinit var listener: OnItemEventListener

    private var tasks = emptyList<Task>() // Cached copy of words

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskItemView: TextView = itemView.findViewById(R.id.toDoListItemTextview)
        val mTimeTextView: TextView = itemView.findViewById(R.id.todoListItemTimeTextView)
        val mColorImageView: ImageView = itemView.findViewById(R.id.toDoListItemColorImageView)
        val rootView: LinearLayout = itemView.findViewById(R.id.listItemLinearLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.task_list_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = tasks[position]

        if (current.hasReminder && current.time != null) {
            holder.taskItemView.maxLines = 1
            holder.mTimeTextView.visibility = View.VISIBLE
        } else {
            holder.taskItemView.maxLines = 2
            holder.mTimeTextView.visibility = View.GONE
        }

        holder.taskItemView.text = current.name

        val myDrawable = TextDrawable.builder().beginConfig()
            .textColor(Color.WHITE)
            .useFont(Typeface.DEFAULT)
            .toUpperCase()
            .endConfig()
            .buildRound(current.name.substring(0, 1), current.color)

        holder.mColorImageView.setImageDrawable(myDrawable)
        current.time?.let { time ->
            holder.mTimeTextView.text = if (is24HourFormat(context)) TimeUtils.formatDate(
                DATE_TIME_FORMAT_24_HOUR,
                time
            ) else TimeUtils.formatDate(DATE_TIME_FORMAT_12_HOUR, time)

            var nowDate = Date()
            var reminderDate = current.time

            holder.mTimeTextView.setTextColor(
                if (reminderDate.before(nowDate)) ContextCompat.getColor(
                    context,
                    R.color.grey600
                ) else ContextCompat.getColor(context, R.color.colorAccent)
            )
        }
        holder.rootView.setOnClickListener {
            listener.onItemClick(current)
        }
    }

    internal fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        notifyDataSetChanged()
    }

    override fun getItemCount() = tasks.size

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(tasks, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(tasks, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemRemoved(position: Int) {
        listener.onItemRemoved(task = tasks[position])
    }

}
```

Adapter中的onBindViewHolder设置每一个item显示，根据Task的hasReminder和time值，显示列表item，然后再MainActivity中，设置Recyclerview和Adapter


```
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.activity_main)
        val adapter = TaskListAdapter(this)
        recyclerview.adapter = adapter
        recyclerview.layoutManager = LinearLayoutManager(this)
        recyclerview.itemAnimator = DefaultItemAnimator()
        recyclerview.setHasFixedSize(true)

        val itemTouchHelperClass = ItemTouchHelperClass(adapter)
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperClass)
        itemTouchHelper.attachToRecyclerView(recyclerview)
    }
}
```

## 连接数据

在MainActivity，创建一个成员变量ViewModel

```
    private lateinit var wordViewModel: TaskViewModel

```
然后我们要实例化它，然后获取了TaskViewModel对象之后，就可以监听Room中Task列表变化。代码如下


```
    wordViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)
    // 在getAllTask返回的LiveData上添加观察者。
    // 当观察到的数据更改并且Acticity处于前台时，将触发onChanged（）方法。
     wordViewModel.allWords.observe(this, Observer { words ->
            // Update the cached copy of the words in the adapter.
            words?.let {
                if (it.isEmpty()) {
                    toDoEmptyView.visibility = View.VISIBLE
                    recyclerview.visibility = View.GONE
                } else {
                    toDoEmptyView.visibility = View.GONE
                    recyclerview.visibility = View.VISIBLE
                    adapter.setTasks(it)
                }
            }
        })

```

当监听列表数据不为空时，recyclerview显示，toDoEmptyView隐藏，否则，toDoEmptyView显示，recyclerview隐藏。然后运行程序，如下图所示

<html>
 <img src="http://lbz-blog.test.upcdn.net/post/todoapp_empty.jpg" width = "180" height = "390"  border="1"  />
</html>

## 添加Task

新建一个***AddTaskActivity***，页面布局**activity_add_task.*xml***如下


```
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:orientation="vertical">

    <EditText
        android:id="@+id/edit_task"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="@dimen/big_padding"
        android:fontFamily="sans-serif-light"
        android:hint="@string/hint_task"
        android:inputType="textAutoComplete"
        android:minHeight="@dimen/min_height"
        android:textSize="18sp" />

    <RelativeLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center_vertical"
        android:layout_margin="@dimen/big_padding"
        android:orientation="horizontal">

        <ImageView
            android:id="@+id/alarmTv"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentLeft="true"
            android:layout_centerVertical="true"
            android:src="@drawable/ic_baseline_add_alarm_24" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_centerVertical="true"
            android:layout_marginLeft="10dp"
            android:layout_toRightOf="@+id/alarmTv"
            android:text="@string/remind_me"
            android:textColor="@color/secondary_text"
            android:textSize="18sp" />

        <com.google.android.material.switchmaterial.SwitchMaterial
            android:id="@+id/switch_btn"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_alignParentRight="true"
            android:layout_centerVertical="true" />

    </RelativeLayout>

    <LinearLayout
        android:visibility="gone"
        android:id="@+id/toDoEnterDateLinearLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="@dimen/big_padding"
        android:animateLayoutChanges="true"
        android:gravity="center"
        android:orientation="vertical">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:gravity="top">

            <EditText
                android:textColor="@color/secondary_text"
                android:text="今天"
                android:id="@+id/newTodoDateEditText"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1.5"
                android:editable="false"
                android:focusable="false"
                android:focusableInTouchMode="false"
                android:gravity="center"
                android:textIsSelectable="false" />

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight=".2"
                android:gravity="center"
                android:padding="4dp"
                android:text="\@"
                android:textColor="?attr/colorAccent" />

            <EditText
                android:textColor="@color/secondary_text"
                android:text="下午1:00"
                android:id="@+id/newTodoTimeEditText"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:editable="false"
                android:focusable="false"
                android:focusableInTouchMode="false"
                android:gravity="center"
                android:textIsSelectable="false" />

        </LinearLayout>

        <TextView
            android:layout_marginTop="10dp"
            android:id="@+id/newToDoDateTimeReminderTextView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="start"
            android:text="@string/remind_date_and_time"
            android:textColor="@color/secondary_text"
            android:textSize="14sp" />

    </LinearLayout>


    <Button
        android:id="@+id/button_save"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="@dimen/big_padding"
        android:background="@color/colorPrimary"
        android:text="@string/button_save"
        android:textColor="@color/buttonLabel" />

</LinearLayout>
```

一个输入Task Name的文本输入框edit_task,一个控制是否需要提醒功能的开关SwitchMaterial，点击文本框newTodoDateEditText弹出一个日期选择器DatePickerDialog，点击文本框newToDoDateTimeReminderTextView弹出一个时间选择器TimePickerDialog，点击保存按钮，如果输入框文本为空，则提示需要输入，否则，创建Task成功，然后退出本Activity，在MainActivity显示刚刚加入的Task。

在AddTaskActivity我们同样需要使用TaskViewModel，让它执行插入操作。

```
class AddTaskActivity : AppCompatActivity() {

    private lateinit var wordViewModel: TaskViewModel

    public override fun onCreate(savedInstanceState: Bundle?) {

        wordViewModel = ViewModelProvider(this).get(TaskViewModel::class.java)

        button_save.setOnClickListener {
            saveTask()
        }

        switch_btn.setOnCheckedChangeListener { _, isChecked ->
            toDoEnterDateLinearLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        newTodoDateEditText.setOnClickListener {
            openDataSelectDialog()
        }

        newTodoTimeEditText.setOnClickListener {
            openTimeSelectDialog()
        }
    }


    private fun saveTask() {
        if (!TextUtils.isEmpty(edit_task.text)) {
            val name = edit_task.text.toString()
            val task = Task(
                name,
                "",
                mUserReminderDate,
                switch_btn.isChecked,
                ColorGenerator.MATERIAL.randomColor
            )
            wordViewModel.insert(task)
            if (switch_btn.isChecked) {
                createNotifyWork(task)
            }
            finish()
        } else {
            Toast.makeText(
                applicationContext,
                R.string.empty_not_saved,
                Toast.LENGTH_LONG
            ).show()
        }
    }


}
```

以上，为AddTaskActivity的关键代码，现在，我们已经完成了对于Task的增删查操作。已经掌握了Room结合Android架构组件开发的流程。现在我们使用Jetpack的另一个组件---WorkManager,令这个程序更有趣一些。

## 对Task带有提醒功能的WorkManager

现在，我们使用WorkManager，对一些有提醒的任务进行系统的提醒（Notification）

1. 第一步，在build.gradle(Module:app)中添加对workmanager的支持


```
  //workManager
    def work_version = "2.3.4"
    implementation "androidx.work:work-runtime-ktx:$work_version"
```

2. 第二步，新建一个继承***Worker***的任务类，我们命名为***NotifyWork***,并重写***doWork*()**方法

```
    override fun doWork(): Result {
        val id = inputData.getInt(NOTIFICATION_ID, 0)
        val title = inputData.getString(TASK_TITLE) ?: "Title"
        sendNotification(id, title)
        return Result.success()
    }
```

3. 实现sendNotification方法，发送系统通知

```
private fun sendNotification(id: Int, title: String) {
        val intent = Intent(applicationContext, AddTaskActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(NOTIFICATION_ID, id)

        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val subtitleNotification = "点击可进入Task详情"
        val pendingIntent = getActivity(applicationContext, 0, intent, 0)
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title).setContentText(subtitleNotification)
            .setDefaults(DEFAULT_ALL).setContentIntent(pendingIntent).setAutoCancel(true)

        notification.priority = PRIORITY_MAX

        if (SDK_INT >= O) {
            notification.setChannelId(NOTIFICATION_CHANNEL)

            val ringtoneManager = getDefaultUri(TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder().setUsage(USAGE_NOTIFICATION_RINGTONE)
                .setContentType(CONTENT_TYPE_SONIFICATION).build()

            val channel =
                NotificationChannel(NOTIFICATION_CHANNEL, NOTIFICATION_NAME, IMPORTANCE_HIGH)

            channel.enableLights(true)
            channel.lightColor = RED
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channel.setSound(ringtoneManager, audioAttributes)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(id, notification.build())
    }

```

4. 在创建任务的时候，如果选择了提醒时间，那么需要创建一个发送系统通知的work，我们在AddTaskActivity执行SaveTask()的时候，补充如下


```
   private fun saveTask() {
        if (!TextUtils.isEmpty(edit_task.text)) {
            ...
            wordViewModel.insert(task)
            if (switch_btn.isChecked) {
                createNotifyWork(task)
            }
            finish()
        }
        ...
    }

    private fun createNotifyWork(task: Task) {
        val customTime = mUserReminderDate.time
        val currentTime = currentTimeMillis()
        if (customTime > currentTime) {
            val data = Data.Builder().putInt(NOTIFICATION_ID, (0 until 100000).random())
                .putString(TASK_TITLE, task.name).build()
            val delay = customTime - currentTime
            scheduleNotification(delay, data,task)
        }
    }

    private fun scheduleNotification(delay: Long, data: Data,task: Task) {
        val notificationWork = OneTimeWorkRequest.Builder(NotifyWork::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()
        task.work_manager_uuid = notificationWork.id.toString()
        wordViewModel.updateWorkIdByName(notificationWork.id.toString(),task.name)
        val instanceWorkManager = WorkManager.getInstance(this)
        instanceWorkManager.beginWith(notificationWork).enqueue()
    }

```


我们为每一个带有提醒时间的Task添加OneTimeWorkRequest。在实体类Task中添加一个字段***work_manager_uuid***保存OneTimeWorkRequest，方便执行列表左滑右滑的时候删除item时候，同时使用cancelWorkById()把对应的任务取消，下面的代码就是MainActivity中item左滑右滑的回调监听。


```
  adapter.setOnItemEventListener(object : TaskListAdapter.OnItemEventListener {
            override fun onItemRemoved(task: Task) {
                Toast.makeText(baseContext, "删除" + task.name + "成功", Toast.LENGTH_SHORT).show()
                wordViewModel.remove(task)
                if (!TextUtils.isEmpty(task.work_manager_uuid)) {
                    WorkManager.getInstance (this@MainActivity)
                        .cancelWorkById(UUID.fromString(task.work_manager_uuid))
                }
            }
        })
```

计算出现在时间和创建Task那个提醒时间的delay差值，使用
```
OneTimeWorkRequest.Builder(NotifyWork::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()
```
来建议一个任务，然后beginWith(notificationWork).enqueue()来把任务交给WorkManager。

这样就实现了当提醒时间到达的时候，系统就会打开一个通知。完成这个提醒功能。

注意：用Google Nexus 6P和小米9分别测试该功能。在杀死app的情况下，前者依旧能够收到系统的通知。但是小米不可以，国产的部分ROM已经对WorkManager失去作用。

## 总结

以上就是基于Android架构组件用Room和WorkManager实现的一个简单TODO APP，基本能掌握ROOM和WorkManager的基础用法，同时对Kotlin的语法有进一步加深理解。

项目地址：https://github.com/laibinzhi/TodoApp














