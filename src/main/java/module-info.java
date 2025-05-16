import org.jspecify.annotations.NullMarked;

@NullMarked
module edu.kit.kastel.vads.compiler {
  requires info.picocli;
  requires org.jspecify;
  requires java.xml;

  opens edu.kit.kastel.vads.compiler to
      info.picocli;
}
