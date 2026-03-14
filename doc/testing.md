# テスト方針

**プロジェクト名**: Flutter Accessible String
**最終更新**: 2026-03-14

---

## TDD (Red-Green-Refactor)

テストリストを先に書き、失敗するテストから着手する。

## ユニットテストのみ

IntelliJ Platform の起動を必要とするテスト（Light Tests, UI Tests）は導入しない。

**テスト対象外**: IntelliJ Platform API を直接呼ぶグルーコード（`IntelliJNotifier`, `IntelliJVfsRefresher`, `DefaultGenerateInvoker`, `StartupActivity`）。それ以外は全てテスト対象とする。

## C1 カバレッジ 100%

テスト対象クラスの全分岐をカバーする。

## 検証項目

各テストでは以下を検証する。

| # | 検証項目 |
|---|---------|
| 1 | テスト対象関数の戻り値 |
| 2 | テスト対象関数の状態 |
| 3 | 下位モジュールへの委譲（DI あり）→ mock で呼び出しを検証 |
| 4 | DI できないもの（ファイル出力等）→ 出力物（ファイル内容）を検証 |
