# Simple Networking SDK

Production-grade Android networking SDK built in Kotlin on top of Retrofit, OkHttp, Coroutines, and Flow.

## Installation

Add JitPack to your root repository list: [![](https://jitpack.io/v/imrun/SimpleNetworking.svg)](https://jitpack.io/#imrun/SimpleNetworking)


```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Add the SDK dependency:

```kotlin
dependencies {
    implementation("com.github.imrun.SimpleNetworking:network-sdk:<tag>")
}
```

Replace `<tag>` with a GitHub release tag such as `1.0.0`.

## Publish on JitPack

1. Commit and push the repository to GitHub.
2. Create a version tag, for example `1.0.0`.
3. Push the tag with `git push origin 1.0.0`.
4. Open `https://jitpack.io/#imrun/SimpleNetworking` and build that tag.
5. After the build succeeds, consume `com.github.imrun.SimpleNetworking:network-sdk:1.0.0`.

## Folder structure

```text
SimpleNetworking
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── README.md
└── network-sdk
    ├── build.gradle.kts
    ├── consumer-rules.pro
    ├── proguard-rules.pro
    └── src/main
        ├── AndroidManifest.xml
        └── java/com/simplenetworking/sdk
            ├── auth
            │   └── TokenAuthenticator.kt
            ├── core
            │   ├── ApiServiceFactory.kt
            │   ├── NetworkClient.kt
            │   └── NetworkConfig.kt
            ├── error
            │   ├── ApiErrorResponse.kt
            │   ├── ErrorMapper.kt
            │   └── NetworkException.kt
            ├── interceptor
            │   ├── ErrorInterceptor.kt
            │   ├── HeaderInterceptor.kt
            │   ├── LoggingInterceptor.kt
            │   ├── RateLimitInterceptor.kt
            │   ├── RetryInterceptor.kt
            │   └── TokenInterceptor.kt
            ├── model
            │   └── ApiErrorResponse.kt
            ├── monitor
            │   └── NetworkMonitor.kt
            ├── pagination
            │   ├── PagingState.kt
            │   └── Paginator.kt
            ├── result
            │   └── ApiResult.kt
            └── util
                ├── FlowApiCall.kt
                └── SafeApiCall.kt
└── sample-app
    ├── build.gradle.kts
    └── src/main
        ├── AndroidManifest.xml
        ├── java/com/simplenetworking/sample
        │   ├── MainActivity.kt
        │   ├── MainViewModel.kt
        │   ├── SampleApplication.kt
        │   └── data
        │       ├── PostDto.kt
        │       ├── SampleApi.kt
        │       └── SampleRepository.kt
        └── res
            ├── layout/activity_main.xml
            └── values
                ├── strings.xml
                └── themes.xml
```

## Initialization

```kotlin
NetworkClient.initialize(
    baseUrl = "https://api.example.com/",
    debug = BuildConfig.DEBUG,
    connectTimeout = 30,
    readTimeout = 30,
    headersProvider = {
        mapOf(
            "Accept" to "application/json",
            "X-App-Version" to "1.0.0"
        )
    },
    tokenProvider = { sessionManager.accessToken },
    refreshTokenProvider = { authRepository.refreshToken() },
    retryCount = 3,
    rateLimitRequests = 10,
    rateLimitWindowSeconds = 1
)

NetworkClient.attachNetworkMonitor(applicationContext)
```

## Create an API service

```kotlin
interface UserApi {
    @GET("users/profile")
    suspend fun getProfile(): Response<UserDto>
}

val userApi = NetworkClient.createService<UserApi>()
```

## Safe API calls

```kotlin
suspend fun loadProfile(): ApiResult<UserDto> {
    return NetworkClient.safeApiCall {
        userApi.getProfile()
    }
}
```

## Flow API calls

```kotlin
fun observeProfile(): Flow<ApiResult<UserDto>> {
    return NetworkClient.flowApiCall {
        userApi.getProfile()
    }
}
```

## Result handling

```kotlin
viewModelScope.launch {
    observeProfile().collect { result ->
        when (result) {
            is ApiResult.Loading -> showLoading()
            is ApiResult.Success -> render(result.data)
            is ApiResult.Empty -> showEmptyState()
            is ApiResult.Error -> showError(result.exception.message.orEmpty())
        }
    }
}
```

## Sample app

The included `sample-app` module shows a real consumer integration:

- `SampleApplication` initializes `NetworkClient` with a base URL, debug flag, headers provider, and connectivity monitor.
- `SampleApi` defines a standard Retrofit interface.
- `SampleRepository` wraps the transport call behind an app-facing boundary.
- `MainViewModel` collects `Flow<ApiResult<PostDto>>`.
- `MainActivity` renders loading, success, empty, and error states.
- `AuthDemoActivity` shows bearer-token wiring through `tokenProvider` and a sample `refreshTokenProvider`.

Open the `sample-app` module in Android Studio and run it on an emulator or device. Tapping `Fetch Post` calls `https://jsonplaceholder.typicode.com/posts/1` through the SDK.
Open `Auth Demo` to inspect the current bearer token, trigger an authenticated request, expire the local token, and manually invoke the refresh lambda. The sample backend does not return `401`, so automatic authenticator retry is configured in code but not triggered by this public endpoint.

## Pagination

```kotlin
val paginator = Paginator<UserDto> { page, cursor ->
    val response = userApi.getUsers(page = page ?: 1, cursor = cursor)
    val body = response.body() ?: error("Missing response body")
    Paginator.PaginationResult(
        items = body.items,
        endReached = body.nextPage == null && body.nextCursor == null,
        nextPage = body.nextPage,
        nextCursor = body.nextCursor
    )
}
```

## Error handling strategy

- `debug = true` preserves backend response bodies, HTTP codes, and throwable details where available.
- `debug = false` maps errors into simplified user-safe messages such as `No Internet Connection`, `Network Timeout`, `Server Error`, `Unauthorized`, and `Unexpected Error`.
- `TokenAuthenticator` retries one failed request after calling the configured refresh lambda on `401`.
- `RetryInterceptor` performs exponential backoff retries for transient I/O failures.
- `RateLimitInterceptor` blocks bursts that exceed the configured request window.

## Local run

Install the sample app with:

```bash
./gradlew :sample-app:installDebug
```
