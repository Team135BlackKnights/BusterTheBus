// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.PWMVictorSPX;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;


public class Robot extends TimedRobot {
  private DifferentialDrive m_myRobot;
  private XboxController m_controller = new XboxController(0);;

  private final PWMVictorSPX m_leftMotor = new PWMVictorSPX(4);
  private final PWMVictorSPX m_rightMotor = new PWMVictorSPX(5);

  private final Servo eyebrowLeft = new Servo(0);
  private final Servo stopArm = new Servo(1);
  private final Servo eyebrowRight = new Servo(2);
  private final Servo mysteryThing = new Servo(3);

  private final PWMVictorSPX yellowLight = new PWMVictorSPX(6);
  private final PWMVictorSPX redLight = new PWMVictorSPX(7);

  private final Timer flashTimer = new Timer();
  private boolean flashYellow = false;

  private boolean eyebrowLeftUp = false;
  private boolean eyebrowRightUp = false;
  private boolean stopArmOut = false;
  private boolean mysteryExtended = false;
  private Trigger aButtonTrigger = new Trigger(m_controller::getAButton);
  private Trigger bButtonTrigger = new Trigger(m_controller::getBButton);
  private Trigger xButtonTrigger = new Trigger(m_controller::getXButton);
  private Trigger leftBumperTrigger = new Trigger(m_controller::getLeftBumper);

  @Override
  public void robotInit() {
    m_myRobot = new DifferentialDrive(m_leftMotor, m_rightMotor);
    m_rightMotor.setInverted(true);


    // Lights toggle on A
    aButtonTrigger.whenActive(new InstantCommand(() -> {flashYellow = true; System.out.println("hi");}))
        .whenInactive(new InstantCommand(() -> flashYellow = false));

    // Servo button bindings
    aButtonTrigger
        .whenActive(new InstantCommand(() -> toggleServo(eyebrowLeft, () -> eyebrowLeftUp, v -> eyebrowLeftUp = v)));
    bButtonTrigger
        .whenActive(new InstantCommand(() -> toggleServo(eyebrowRight, () -> eyebrowRightUp, v -> eyebrowRightUp = v)));
    xButtonTrigger.whenActive(new InstantCommand(() -> toggleServo(stopArm, () -> stopArmOut, v -> stopArmOut = v)));
    leftBumperTrigger.whenActive(
        new InstantCommand(() -> toggleServo(mysteryThing, () -> mysteryExtended, v -> mysteryExtended = v)));

    flashTimer.start();
  }

  @Override
  public void teleopPeriodic() {
    // Tank drive
    double leftPower = -m_controller.getLeftY();
    double rightPower = -m_controller.getRightY();
    m_myRobot.tankDrive(leftPower, rightPower);


    if (!flashTimer.advanceIfElapsed(0.5)) {
      return;
    }

    // Flashing lights
    if (flashYellow) {
      redLight.set(0);
      double current = yellowLight.get();
      if (Math.abs(current) > 0.5) {
        yellowLight.set(0.0);
      } else {
        yellowLight.set(1.0);
      }
    } else {
      yellowLight.set(0);
      double current = redLight.get();
      if (Math.abs(current) > 0.5) {
        redLight.set(0.0);
      } else {
        redLight.set(1.0);
      }

    }
  }

  private void toggleServo(Servo servo, java.util.function.Supplier<Boolean> getter,
      java.util.function.Consumer<Boolean> setter) {
    boolean state = !getter.get();
    setter.accept(state);
    servo.set(state ? 1.0 : 0.0); // 1.0 = extended, 0.0 = retracted
  }
}
