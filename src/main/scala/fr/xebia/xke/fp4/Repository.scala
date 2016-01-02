package fr.xebia.xke.fp4

trait Repository[Id, Resource] {

  private var data = Map[Id, Resource]()

  def find(id: Id): Option[Resource] = {
    data.get(id)
  }

  def add(entry: (Id, Resource)): Unit = {
    data += entry

  }

}