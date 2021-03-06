package dungeondagger

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx._
import com.badlogic.gdx.graphics.{Texture, GL20}
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.actions.{DelayAction, SequenceAction, MoveByAction}
import com.badlogic.gdx.scenes.scene2d.{Actor, Stage}

import scala.util.Random

class GameScreen(game: Game) extends DefaultScreen(game) with InputProcessor {
  def path(hexName: String) =
    s"data/hexagonTiles/Tiles/tile$hexName.png"

  println(Gdx.files.getExternalStoragePath)
  val textures = List("Autumn", "Grass", "Lava", "Dirt", "Magic", "Rock", "Sand", "Stone", "Water")
    .map(path)
    .map(Gdx.files.local)
    .map(new Texture(_))
    .toArray

  Gdx.input.setInputProcessor(this)

  val rand = new Random()

  val Height = 15
  val Width = 18

  val map: Array[Int] = Array.fill(Height * Width){ rand.nextInt(6)}

  var person = 0
  val personTexture = new Texture(Gdx.files.internal("data/hexagonTiles/Tiles/alienPink.png"))

  val stage:Stage = new Stage()

  override def dispose() {
    stage.dispose()
    personTexture.dispose()
    textures.map(_.dispose())
  }

  class HexTile(texture: Texture) extends Actor {
    var started = false
    var hasPerson = false
    override def draw(batch:Batch, alpha:Float){
      batch.draw(texture,getX,getY)
      if (hasPerson) {
        batch.draw(personTexture, getX, getY + 35)
      }
    }
  }

  val tileActors = Range(0, Height).map{ i =>
    Range(0, Width).map { j =>
      val tileId = i * Width + j
      val t = textures(map(tileId))
      val x = j * 65 + (i % 2) * 32
      val tile = new HexTile(t)
      tile.setPosition(x,i * 49)
      tile
    }
  }.flatten

  tileActors.reverse foreach stage.addActor

  tileActors(0).hasPerson = true

  def movePerson(toTileId: Int): Unit ={
    tileActors(person).hasPerson = false
    person = toTileId
    tileActors(person).hasPerson = true
  }

  def wobble(): Unit ={
    val center = tileActors(tileActors.size / 2)
    tileActors foreach { a =>
      val dx = center.getX - a.getX
      val dy = center.getY - a.getY
      val r2 = dx * dx + dy * dy
//      val r = Math.sqrt(r2).toFloat
      val there = new MoveByAction()
      val back = new MoveByAction()
      there.setDuration(1.7f)
      back.setDuration(1.7f)
      val delay = new DelayAction()
      delay.setDuration(r2 / 100000)
      there.setInterpolation(Interpolation.circleOut)
      back.setInterpolation(Interpolation.circleIn)
      val h = 10000000 / (50000 + r2)
      there.setAmountY(h)
      back.setAmountY(-h)
      val action = new SequenceAction(delay,there,back)
      a.addAction(action)
    }
  }

  override def render(delta: Float) {
    Gdx.gl.glClearColor(1, 1, 1, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    stage.act(Gdx.graphics.getDeltaTime)
    stage.draw()
  }

  override def keyDown(keycode: Int): Boolean = {
    keycode match {
      case(Input.Keys.UP) if person + Width < Height * Width => movePerson(person + Width)
      case(Input.Keys.DOWN) if person - Width >= 0 => movePerson(person - Width)
      case(Input.Keys.RIGHT) if person % Width != Width - 1 => movePerson(person + 1)
      case(Input.Keys.LEFT) if person % Width != 0 => movePerson(person - 1)
      case _ => wobble()
    }
    true
  }

  override def mouseMoved(screenX: Int, screenY: Int): Boolean = false

  override def keyTyped(character: Char): Boolean = false

  override def touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false

  override def keyUp(keycode: Int): Boolean = false

  override def scrolled(amount: Int): Boolean = false

  override def touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false

  override def touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = false
}
