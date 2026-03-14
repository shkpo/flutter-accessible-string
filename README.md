# Flutter Accessible String

flutter_intl が生成した `l18n.dart`から、UI 表記用文字列とスクリーンリーダー読み上げ用文字列のペアを生成する Android Studio のプラグインです。

## 背景

同一の ARB ファイルに混在している UI 表記用文字列（例: `hoge`）とスクリーンリーダー読み上げ用文字列（例: `hogeReader`）の紐づけは手作業となっていた。このプラグインにより文法的な紐づけが実現します。

## 動作フロー

```
ARB ファイルを編集・保存
  → flutter_intl が lib/generated/l18n.dart を更新
  → 本プラグインが l18n.dart の変更を検知
  → ARB ファイルを解析してキーペアを検出
  → lib/generated/accessible/s_accessible.g.dart を生成
```

## セットアップ

### 1. プラグインのビルド

```bash
./gradlew buildPlugin
```

`build/distributions/flutter-accessible-string-1.0.0.zip` が生成されます。

### 2. Android Studio へのインストール

**Settings → Plugins → ⚙️ → Install Plugin from Disk...** から上記の `.zip` を選択してインストール。

### 3. Flutter プロジェクトへの設定ファイル追加

プロジェクトルートに `accessible_gen.yaml` を作成します。

```yaml
accessible_gen:
  arb_dir: lib/l10n                        # ARB ファイルのディレクトリ
  master_locale: ja                        # キー列挙の基準ロケール
  reader_suffix: Reader                    # スクリーンリーダー用キーのサフィックス
  trigger_file: lib/generated/l10n.dart   # 監視対象ファイル（flutter_intl の出力）
  output_dir: lib/generated/accessible    # 生成ファイルの出力先
```

すべての項目にデフォルト値があるため、上記と同じ構成であれば省略も可能です。

```yaml
accessible_gen: {}
```

## ARB ファイルの命名規則

スクリーンリーダー用文字列のキーは `{UIキー}{reader_suffix}` の形式で定義します。

```json
{
  "@@locale": "ja",
  "btnAgreeToTos": "同意",
  "btnAgreeToTosReader": "同意。全文を確認するとタップできます",
  "applicationDeadline": "応募期限：{reg_end_date}",
  "applicationDeadlineReader": "チケットの応募期限：{reg_end_date}",
  "hoge": "ほーじ"
}
```

- `hoge` のように対応する Reader キーがないキーも許容されます
- Reader キー単体（UI キーなし）は生成対象外です

## 生成されるコード

```dart
// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: non_constant_identifier_names

import '../l18n.dart';

class AccessibleString {
  final String label;
  final String semanticsLabel;

  const AccessibleString({
    required this.label,
    String? semanticsLabel,
  }) : semanticsLabel = semanticsLabel ?? label; // Reader なしの場合は label を使用
}

extension SAccessible on S {
  // ペアあり
  AccessibleString get btnAgreeToTos => AccessibleString(
    label: btnAgreeToTos,
    semanticsLabel: btnAgreeToTosReader,
  );

  // ペアなし（semanticsLabel は label にフォールバック）
  AccessibleString get hoge => AccessibleString(
    label: hoge,
  );

  // 引数あり
  AccessibleString applicationDeadline(dynamic deadline) => AccessibleString(
    label: applicationDeadline(deadline),
    semanticsLabel: applicationDeadlineReader(deadline),
  );
}
```

## 使い方（Flutter コード側）

```dart
// 生成前（手実装）
Semantics(
  label: S.of(context).btnAgreeToTosReader,
  child: Text(S.of(context).btnAgreeToTos),
)

// 生成後
final str = S.of(context).btnAgreeToTos;
Semantics(
  label: str.semanticsLabel,
  child: Text(str.label),
)
```

## 手動実行

**Tools → Flutter Accessible String → Generate Accessible Strings** から手動で生成を実行できます。

## 要件

- Android Studio Jellyfish (2023.3.1) 以降
- flutter_intl プラグインがインストール済みであること
- `accessible_gen.yaml` がプロジェクトルートに存在すること

## ライセンス

Private
