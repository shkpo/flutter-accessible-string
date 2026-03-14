# 開発プロセス

**プロジェクト名**: Flutter Accessible String
**最終更新**: 2026-03-14

---

## 作業開始前に読むドキュメント

| ドキュメント | 内容 |
|-------------|------|
| [architecture.md](architecture.md) | 設計方針・技術選定 |
| [testing.md](testing.md) | テスト方針・検証項目 |

---

## 実装フロー

**いずれかのフェーズが失敗したら、TDD から最初からやり直す。**

| # | フェーズ | コマンド | 備考 |
|---|---------|---------|------|
| 1 | TDD (Red→Green→Refactor) | `./gradlew test` | テストリストを先に書き、失敗するテストから着手。warning も error 扱いのため TDD 中に解決する |
| 2 | フォーマット | `./gradlew ktlintFormat` | |
| 3 | lint | `./gradlew ktlintCheck` | |
| 4 | 全体テスト実行 | `./gradlew test` | 全テストがグリーンであること |
| 5 | ビルド | `./gradlew buildPlugin` | ビルド成功を確認 |
| 6 | git commit | `git commit` | コミットメッセージ形式は [Git コミットメッセージ](#git-コミットメッセージ) を参照 |

---

## コマンド

### ビルド

```bash
./gradlew buildPlugin
```

`build/distributions/flutter-accessible-string-1.0.0.zip` が生成される。

### テスト実行

```bash
./gradlew test
```

### lint

```bash
# TBD
```

---

## Git コミットメッセージ

TBD
