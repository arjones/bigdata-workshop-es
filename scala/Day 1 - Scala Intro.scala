// Databricks notebook source
// MAGIC %md
// MAGIC ### `val` or `var`

// COMMAND ----------

val city = "Buenos Aires"

// COMMAND ----------

city = "São Paulo"

// COMMAND ----------

val sum = 1+2+3

// COMMAND ----------

var a = 10
a=a+1
a

// COMMAND ----------

val b = 10
val c = b + a

// COMMAND ----------

// MAGIC %md
// MAGIC ## Functions & Apply

// COMMAND ----------

def sumar1(valor: Int) = valor + 1
sumar1(10)

// COMMAND ----------

val nums = List(1,2,5,7,9, 12, 14)
nums.map(n => n+1)
// nums.map(_+1)
// nums.map(_ + 1)

// COMMAND ----------

nums.map(n => sumar1(n))
// nums.map(sumar1(_))
// nums.map(sumar1)
// nums map sumar1

// COMMAND ----------

// MAGIC %md ### Filter

// COMMAND ----------

nums.filter(_ % 2 == 0)

// COMMAND ----------

def filtrar(valor: Int): Boolean = {
if(valor >= 9)
  return true
else
  return false
}

filtrar(10)

// COMMAND ----------

nums.filter(n => filtrar(n))


// COMMAND ----------

nums.reduce((a, b) => a + b)

// nums.reduce(_ + _)

// nums.sum

// COMMAND ----------

nums.map(_.toString).reduce((a, b) => a + b)
// nums.map(_.toString).reduce((a, b) => a + " > " + b)

// COMMAND ----------

val name = "gustavo arjones"
def test(c: Char):Boolean = c.isUpper

name.exists(_.isUpper)

// COMMAND ----------

// MAGIC %md
// MAGIC ## Case Class

// COMMAND ----------

case class Person(name: String, age: Int, gender: Char, country: String)

// COMMAND ----------

val p1 = Person("Gustavo", 41, 'M', "Brasil")
val p2 = Person("Laura", 25, 'F', "Argentina")

// COMMAND ----------

p1.name

// COMMAND ----------



// COMMAND ----------

case class Language(name: String) {
  def hello = "Hola, " + name
}

// COMMAND ----------

val lang = Language("English")
lang.hello

// COMMAND ----------

val lang2 = Language("English")

// COMMAND ----------

lang == lang2

// COMMAND ----------

val persons = List(
  Person("Gustavo", 41, 'M', "Brasil"),
  Person("Paula", 30, 'F', "Argentina"),
  Person("Fernanda", 12, 'F', "Paraguay"),
  Person("Olivia", 19, 'F', "Colombia"),
  Person("Juan", 55, 'M', "Argentina")
)

def isAdult(age: Int) = age >= 18

persons.filter(p => isAdult(p.age))

// COMMAND ----------

def mujerYMayores(p: Person) = {
  p.gender == 'F' && p.age >= 20
}

persons.filter(mujerYMayores)
persons.map(p => p.country)
val paises = persons.map(p => p.country)



// COMMAND ----------

paises.distinct

// COMMAND ----------

paises.groupBy(pais => pais)

// COMMAND ----------

// MAGIC %md
// MAGIC ### Pattern Match

// COMMAND ----------

personas.groupBy {
  case Person(name, age, gender, country) => country
}

personas.groupBy {
  case Person(_, _, _, country) => country
}

// COMMAND ----------

personas.groupBy {
  case Person(_,_, 'F', country) => country.toUpperCase
  case Person(_,_, 'M', country) => country
}

// COMMAND ----------

val num = 12
val res = if(num > 10) "si" else "no"

// COMMAND ----------

// MAGIC %md ### Using `Try`

// COMMAND ----------

val numbers = List(1, 42, 0, 23, 17, 10)

// COMMAND ----------

numbers.map( e => 10 / e )

// COMMAND ----------

import scala.util.Try
val res = numbers.map( e=> Try(10 / e) )


// COMMAND ----------

res.filter(_.isSuccess).map(_.get)

// COMMAND ----------

res.map(_.toOption).flatten

// COMMAND ----------


