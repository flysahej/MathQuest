package comp2800_project;

import java.util.Random;

public class QuestionGenerator {
    private Random random;
    private int level;
    
    public QuestionGenerator(int level) {
        this.level = level;
        this.random = new Random();
    }
    
    public Question generateQuestion(int row) {
        switch(row) {
            case 0: return generateBasicQuestion();
            case 1: return generateSecondaryQuestion();
            case 2: return generateAdvancedQuestion();
            default: return generateBasicQuestion();
        }
    }
    
    private Question generateBasicQuestion() {
        int difficulty = level + 1;
        int type = random.nextInt(2); // 0=add, 1=subtract
        
        if (type == 0) {
            int a = random.nextInt(10 * difficulty) + 1;
            int b = random.nextInt(10 * difficulty) + 1;
            String question = a + " + " + b + " = ?";
            return new Question(question, String.valueOf(a + b));
        } else {
            int a = random.nextInt(10 * difficulty) + 10;
            int b = random.nextInt(10 * difficulty) + 1;
            if (a < b) {
                int temp = a;
                a = b;
                b = temp;
            }
            String question = a + " - " + b + " = ?";
            return new Question(question, String.valueOf(a - b));
        }
    }
    
    private Question generateSecondaryQuestion() {
        int difficulty = level + 1;
        int type = random.nextInt(2); // 0=multiply, 1=divide
        
        if (type == 0) {
            int a = random.nextInt(10 * difficulty) + 1;
            int b = random.nextInt(10 * difficulty) + 1;
            String question = a + " × " + b + " = ?";
            return new Question(question, String.valueOf(a * b));
        } else {
            int b = random.nextInt(5 * difficulty) + 1;
            int a = b * (random.nextInt(10 * difficulty) + 1);
            String question = a + " ÷ " + b + " = ?";
            return new Question(question, String.valueOf(a / b));
        }
    }
    
    private Question generateAdvancedQuestion() {
        int difficulty = level + 1;
        int type = random.nextInt(2);
        
        if (type == 0) {
            // Linear equation: ax + b = c
            int a = random.nextInt(difficulty * 2) + 1;
            int b = random.nextInt(difficulty * 10) + 1;
            int c = random.nextInt(difficulty * 20) + 1;
            String question = "Solve: " + a + "x + " + b + " = " + c;
            double answer = (c - b) / (double) a;
            String answerStr = String.format("%.2f", answer);
            return new Question(question, answerStr);
        } else {
            // Quadratic: ax² + bx + c = 0 (simplified)
            int a = random.nextInt(3) + 1;
            int b = random.nextInt(10) + 1;
            int c = random.nextInt(10) + 1;
            String question = "Quadratic: " + a + "x² + " + b + "x + " + c + " = 0";
            return new Question(question, "Use quadratic formula");
        }
    }
    
    public class Question {
        private String text;
        private String answer;
        
        public Question(String text, String answer) {
            this.text = text;
            this.answer = answer;
        }
        
        public String getText() { return text; }
        public String getAnswer() { return answer; }
    }
}
