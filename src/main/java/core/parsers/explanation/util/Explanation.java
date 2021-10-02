package core.parsers.explanation.util;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import core.commands.utils.serializers.ExplanationSerializer;

@FunctionalInterface
@JsonSerialize(using = ExplanationSerializer.class)
public interface Explanation {

    Interactible explanation();


}
