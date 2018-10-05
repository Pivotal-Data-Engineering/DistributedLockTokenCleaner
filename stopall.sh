# bring down servers in descending order as a discipline for advanced restarting

gfsh <<!

connect 

shutdown --include-locators=true
Y
exit;
!

