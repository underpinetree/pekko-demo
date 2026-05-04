# はまった点まとめ

## 1. Pekko 設定が application.yaml から読まれない

### 症状
```
ActorSystem needs to have 'pekko.actor.provider' set to 'cluster'
```

### 原因
`ConfigFactory.load()` は Spring の `application.yaml` ではなく HOCON 形式の `application.conf` を読む。

### 対処
`src/main/resources/application.conf` を別途作成して Pekko 設定を書く。

---

## 2. マルチノードでメッセージのシリアライズエラー

### 症状
```
No configured serialization-bindings for class [VoiceSessionActor$Join]
```

### 原因
シングルノードではメッセージはメモリ内で渡るだけなので問題ない。マルチノードでは別 JVM にネットワーク経由で送るためシリアライズが必要。

### 対処
`Command` に `Serializable` を実装し、`application.conf` で Java シリアライゼーションを有効にする。

```kotlin
sealed interface Command : Serializable
```

```hocon
pekko.actor {
  allow-java-serialization = on
  warn-about-java-serializer-usage = off
}
```

---

## 3. pekko-management が pekko-discovery の古いバージョンを引き込む

### 症状
```
You are using version 1.1.3 of Apache Pekko, but it appears you also depend on older versions.
(1.0.1, [pekko-discovery]), (1.1.3, [pekko-actor, ...])
```

### 原因
`pekko-management 1.0.0` が内部で `pekko-discovery 1.0.1` を依存として持っており、メインの `1.1.3` と混在する。

### 対処
`pekko-discovery 1.1.3` を明示的に依存に追加して上書きする。

```kotlin
implementation("org.apache.pekko:pekko-discovery_3:$pekkoVersion")
```

---

## 4. Cluster Bootstrap が seed-nodes の設定があると機能しない

### 症状
```
Application is configured with specific `pekko.cluster.seed-nodes`, bailing out of the bootstrap process!
```

### 原因
`application-k8s.conf` が `application.conf` を include しているため `seed-nodes = ["pekko://...@127.0.0.1:25520"]` が残ってしまい、Cluster Bootstrap が無効化される。

### 対処
`application-k8s.conf` で seed-nodes を空に上書きする。

```hocon
pekko.cluster.seed-nodes = []
```

---

## 5. Management の hostname を 0.0.0.0 にするとクラスタ形成できない

### 症状
```
Self contact point [0.0.0.0:8558] not found in targets [10.244.0.x, ...]
```

### 原因
Cluster Bootstrap は Pod 同士が自分の IP を広告し合ってクラスタを形成する。`hostname = "0.0.0.0"` は「全インターフェースで待ち受ける」という意味であり、他の Pod への自己紹介アドレスとしては使えない。k8s が発見する Pod 一覧は実 IP なので `0.0.0.0` とマッチしない。

### 対処
バインドアドレスと広告アドレスを分ける。

```hocon
pekko.management.http {
  hostname = ${PEKKO_HOSTNAME}   # 広告する IP（Pod IP）
  bind-hostname = "0.0.0.0"     # 待ち受けるアドレス
}
```

`PEKKO_HOSTNAME` は Deployment の env で Pod IP を注入する。

```yaml
env:
  - name: PEKKO_HOSTNAME
    valueFrom:
      fieldRef:
        fieldPath: status.podIP
```

