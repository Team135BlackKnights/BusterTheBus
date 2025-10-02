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
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;


public class Robot extends TimedRobot {
  private DifferentialDrive drive;
  private XboxController controller = new XboxController(0);;

  private final PWMVictorSPX leftMotor = new PWMVictorSPX(4);
  private final PWMVictorSPX rightMotor = new PWMVictorSPX(5);

  private final Servo eyebrowLeft = new Servo(0);
  private final Servo stopArm = new Servo(1);
  private final Servo eyebrowRight = new Servo(2);
  private final Servo eyesServo = new Servo(3);

  private final PWMVictorSPX yellowLight = new PWMVictorSPX(6);
  private final PWMVictorSPX redLight = new PWMVictorSPX(7);

  private final Timer flashTimer = new Timer();

  private boolean flashYellow = false;

  private boolean eyebrowLeftUp = false;
  private boolean eyebrowRightUp = false;
  private boolean stopArmOut = false;
  private boolean eyeState = false;

  private Trigger aButtonTrigger = new Trigger(controller::getAButton);
  private Trigger yButtonTrigger = new Trigger(controller::getYButton);
  private Trigger bButtonTrigger = new Trigger(controller::getBButton);
  private Trigger xButtonTrigger = new Trigger(controller::getXButton);
  private Trigger leftBumperTrigger = new Trigger(controller::getLeftBumper);

  @Override
  public void robotInit() {
    drive = new DifferentialDrive(leftMotor, rightMotor);
    rightMotor.setInverted(true);


    // Lights toggle on A
    yButtonTrigger
      .whenActive(new InstantCommand(() -> {flashYellow = true;}))
      .whenInactive(new InstantCommand(() -> flashYellow = false));

    // Servo button bindings
    aButtonTrigger
      .whenActive(new InstantCommand(() -> toggleServo(eyebrowLeft, () -> eyebrowLeftUp, v -> eyebrowLeftUp = v)));
    bButtonTrigger
      .whenActive(new InstantCommand(() -> toggleServo(eyebrowRight, () -> eyebrowRightUp, v -> eyebrowRightUp = v)));
    xButtonTrigger
      .whenActive(new InstantCommand(() -> toggleServo(stopArm, () -> stopArmOut, v -> stopArmOut = v)));
    leftBumperTrigger
      .whenActive(new InstantCommand(() -> toggleServo(eyesServo, () -> eyeState, v -> eyeState = v)));

    flashTimer.start();
  }

  @Override
  public void teleopPeriodic() {
    CommandScheduler.getInstance().run();

    // Tank drive
    double leftPower = -controller.getLeftY();
    double rightPower = -controller.getRightY();
    drive.tankDrive(leftPower, rightPower);

    // Update code past this every 0.5 seconds
    if (!flashTimer.advanceIfElapsed(0.5)) {
      return;
    }

    System.out.println(System.currentTimeMillis() / 1000 + ": [LED] toggle flash");

    // Flashing lights
    if (flashYellow) {
      redLight.set(0);

      // double current = yellowLight.get();
      
      // if (Math.abs(current) > 0.5) {
      //   yellowLight.set(0.0);
      // } else {
      //   yellowLight.set(1.0);
      // }

      yellowLight.set(1.0);
    } 
    else {
      yellowLight.set(0.0);

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
    servo.set(state ? 1.0 : -1.0); // 1.0 = extended, -1.0 = retracted
  }
}
