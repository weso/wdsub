package es.weso.wdsub

import munit._
import es.weso.wdsub.fs2processor.RDFSerializer._

class RDFSerializerTest extends FunSuite {

  test("Remove prefixes") {

    val str      = """|@prefix sh: <http://example.org/>
                 |@prefix : <http://pepe.org/>
                 |@prefix foo: <http://foo.org/>
                 |
                 |:x a :T .
                 |""".stripMargin
    val expected = """| :x a :T .
                      |""".stripMargin
    assertNoDiff(removePrefixes(str), expected)
  }
}
