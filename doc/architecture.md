# 設計書

**プロジェクト名**: Flutter Accessible String
**バージョン**: v1.1
**最終更新**: 2026-03-14

---

## 1. 技術選定の理由

| カテゴリ | 採用技術 | 理由 |
|----------|---------|------|
| 言語 | Kotlin | IntelliJ Platform Plugin の事実上の標準言語 |
| モックライブラリ | MockK | Kotlin の data class・object・拡張関数をモック可能。Mockito では困難 |

---

## 2. アーキテクチャ方針

### IntelliJ 依存はインタフェースで隔離する

`INotifier`, `IVfsRefresher`, `IGenerateInvoker` のように、IntelliJ Platform API を直接呼ぶクラスはすべてインタフェース越しに注入する。

**理由**: IntelliJ Platform の起動なしに JUnit + MockK だけでテストを回せるようにするため。IntelliJ Platform の起動を伴うテスト（Light Tests 等）はセットアップコストが高く、CI での実行も重い。純粋なユニットテストで十分カバーできる範囲はそちらで完結させる。

---

## 3. 変更履歴

| 日付 | バージョン | 内容 |
|------|-----------|------|
| 2026-03-14 | v1.0 | 初版作成 |
| 2026-03-14 | v1.1 | テスト方針を testing.md に分離 |
