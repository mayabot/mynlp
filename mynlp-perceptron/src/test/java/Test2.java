public class Test2 {
    public static void main(String[] args) {
        Character c = 'a';

        String text = "你好";

        for (char c1 : text.toCharArray()) {
            Character character = Character.valueOf(c1);
            System.out.println(character.hashCode());
        }

        System.out.println(Character.valueOf('你').hashCode());
    }
}
