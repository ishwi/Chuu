package ish.services.dtos;

import java.util.List;

public record CommandInfo(String name, String category, List<String> aliases, String instructions) {
}
