package com.sksamuel.elastic4s.delete

import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.http.ElasticDsl
import com.sksamuel.elastic4s.testkit.DualClientTests
import com.sksamuel.elastic4s.testkit.ResponseConverterImplicits._
import org.scalatest.{Matchers, WordSpec}

import scala.util.Try

class DeleteByIdTest extends WordSpec with Matchers with ElasticDsl with DualClientTests {

  override protected def beforeRunTests(): Unit = {

    Try {
      execute {
        deleteIndex("lecarre")
      }.await
    }

    execute {
      createIndex("lecarre").mappings(
        mapping("characters").fields(
          textField("name")
        )
      ).shards(1).waitForActiveShards(1)
    }.await
  }

  "delete by id request" should {
    "delete matched docs" in {

      execute {
        indexInto("lecarre" / "characters").fields("name" -> "jonathon pine").id(2).refresh(RefreshPolicy.Immediate)
      }.await

      execute {
        indexInto("lecarre" / "characters").fields("name" -> "george smiley").id(4).refresh(RefreshPolicy.Immediate)
      }.await

      execute {
        search("lecarre" / "characters").matchAllQuery()
      }.await.totalHits shouldBe 2

      execute {
        delete(2).from("lecarre" / "characters").refresh(RefreshPolicy.Immediate)
      }.await

      execute {
        search("lecarre" / "characters").matchAllQuery()
      }.await.totalHits shouldBe 1

      execute {
        delete(4).from("lecarre" / "characters").refresh(RefreshPolicy.Immediate)
      }.await

      execute {
        search("lecarre" / "characters").matchAllQuery()
      }.await.totalHits shouldBe 0
    }
  }
}
