! A fortran95 program for G95
! By Kris Chan
program main
  implicit none
  integer re_i
  integer :: iteTime = 10,i
  real ,dimension(10) :: initNumber
  real ,dimension(10) :: initCandidate
  real :: pi = 3.1415926
  call random_seed ()
  !generate 10 numbers randomly
  do i=1,iteTime,1
    call random_number(initNumber(i))
    initCandidate(i) = -1+2*sin(pi*initNumber(i))
    write(*,*) initCandidate(i)
  end do
  !choose the best initial number in the 10 numbers
  re_i = system("pause")
end
